package com.example.data.repository

import com.example.data.dao.AnalysisDao
import com.example.data.entity.AnalysisEntity
import kotlinx.coroutines.flow.Flow

class AnalysisRepository(private val analysisDao: AnalysisDao) {

    val allAnalyses: Flow<List<AnalysisEntity>> = analysisDao.getAllAnalyses()
    val totalCount: Flow<Int> = analysisDao.getCount()

    suspend fun getAnalysisById(id: String): AnalysisEntity? {
        return analysisDao.getAnalysisById(id)
    }

    fun getAnalysesByModality(mediaType: String): Flow<List<AnalysisEntity>> {
        return analysisDao.getAnalysesByModality(mediaType)
    }

    suspend fun insertAnalysis(analysis: AnalysisEntity) {
        analysisDao.insertAnalysis(analysis)
    }

    suspend fun deleteAnalysisById(id: String) {
        analysisDao.deleteAnalysisById(id)
    }

    suspend fun clearAll() {
        analysisDao.clearAll()
    }
}
