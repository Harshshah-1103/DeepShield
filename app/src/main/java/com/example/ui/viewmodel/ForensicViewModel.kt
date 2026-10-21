package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeepShieldDatabase
import com.example.data.entity.AnalysisEntity
import com.example.data.repository.AnalysisRepository
import com.example.forensics.ForensicAnalyzer
import com.example.forensics.ForensicReportParser
import com.example.forensics.PresetSample
import com.example.forensics.PresetSampleProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ForensicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DeepShieldDatabase.getDatabase(application)
    private val repository = AnalysisRepository(database.analysisDao())
    private val analyzer = ForensicAnalyzer(application)

    // Raw analyses from Room
    val allAnalyses: StateFlow<List<AnalysisEntity>> = repository.allAnalyses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedModality = MutableStateFlow("ALL")
    val selectedModality: StateFlow<String> = _selectedModality.asStateFlow()

    private val _selectedRiskFilter = MutableStateFlow("ALL")
    val selectedRiskFilter: StateFlow<String> = _selectedRiskFilter.asStateFlow()

    // Filtered analyses
    val filteredAnalyses: StateFlow<List<AnalysisEntity>> = combine(
        allAnalyses,
        _searchQuery,
        _selectedModality,
        _selectedRiskFilter
    ) { list, query, modality, riskFilter ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.filename.contains(query, ignoreCase = true) ||
                    item.fileHash.contains(query, ignoreCase = true) ||
                    item.id.contains(query, ignoreCase = true)

            val matchesModality = modality == "ALL" || item.mediaType == modality

            val matchesRisk = when (riskFilter) {
                "HIGH_RISK" -> item.riskScore >= 65
                "INCONCLUSIVE" -> item.riskScore in 36..64 || item.classification == "INCONCLUSIVE"
                "AUTHENTIC" -> item.riskScore <= 35 && item.classification == "LIKELY_AUTHENTIC"
                else -> true
            }

            matchesQuery && matchesModality && matchesRisk
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected analysis for detail report view
    private val _selectedAnalysis = MutableStateFlow<AnalysisEntity?>(null)
    val selectedAnalysis: StateFlow<AnalysisEntity?> = _selectedAnalysis.asStateFlow()

    // Analysis execution state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStepProgress = MutableStateFlow("")
    val analysisStepProgress: StateFlow<String> = _analysisStepProgress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Configuration
    var configElaQuality = MutableStateFlow(90)
    var configFrequencyAnalysis = MutableStateFlow(true)
    var configNoiseResidual = MutableStateFlow(true)
    var configFacialSeam = MutableStateFlow(true)

    // Preset Samples
    val presetSamples: List<PresetSample> = PresetSampleProvider.getSamples()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedModality(modality: String) {
        _selectedModality.value = modality
    }

    fun setSelectedRiskFilter(filter: String) {
        _selectedRiskFilter.value = filter
    }

    fun selectAnalysisById(id: String) {
        viewModelScope.launch {
            val item = repository.getAnalysisById(id)
            _selectedAnalysis.value = item
        }
    }

    fun selectAnalysis(item: AnalysisEntity?) {
        _selectedAnalysis.value = item
    }

    fun analyzePreset(sample: PresetSample, onCompleted: (AnalysisEntity) -> Unit) {
        analyzeFileBytes(sample.sampleBytes, sample.filename, onCompleted)
    }

    fun analyzeFileBytes(
        bytes: ByteArray,
        filename: String,
        onCompleted: (AnalysisEntity) -> Unit
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null
            _analysisStepProgress.value = "Secure ingestion & SHA-256 evidence hashing..."

            try {
                _analysisStepProgress.value = "Executing multi-detector forensic pipelines..."
                val config = ForensicAnalyzer.AnalysisConfiguration(
                    elaSensitivityQuality = configElaQuality.value,
                    runFrequencyAnalysis = configFrequencyAnalysis.value,
                    runNoiseResidual = configNoiseResidual.value,
                    runFacialSeam = configFacialSeam.value
                )

                val result = analyzer.analyzeMedia(bytes, filename, config)
                result.onSuccess { entity ->
                    _analysisStepProgress.value = "Evidence fusion & forensic calibration complete."
                    _selectedAnalysis.value = entity
                    _isAnalyzing.value = false
                    onCompleted(entity)
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Analysis failed."
                    _isAnalyzing.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error executing analysis: ${e.message}"
                _isAnalyzing.value = false
            }
        }
    }

    fun analyzeFromUri(uri: Uri, filename: String, onCompleted: (AnalysisEntity) -> Unit) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null || bytes.isEmpty()) {
                    _errorMessage.value = "Could not read data from selected file."
                    return@launch
                }
                analyzeFileBytes(bytes, filename, onCompleted)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load file: ${e.message}"
            }
        }
    }

    fun deleteAnalysis(id: String) {
        viewModelScope.launch {
            val item = repository.getAnalysisById(id)
            item?.heatmapPath?.let { File(it).delete() }
            item?.elaPath?.let { File(it).delete() }
            item?.noisePath?.let { File(it).delete() }
            repository.deleteAnalysisById(id)
            if (_selectedAnalysis.value?.id == id) {
                _selectedAnalysis.value = null
            }
        }
    }

    fun clearAllAnalyses() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedAnalysis.value = null
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun getExportText(entity: AnalysisEntity): String {
        return ForensicReportParser.generateFormattedForensicReport(entity)
    }

    fun getExportJson(entity: AnalysisEntity): String {
        return ForensicReportParser.exportJsonReport(entity)
    }
}
