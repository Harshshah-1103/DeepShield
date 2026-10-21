package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.AnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analyses ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<AnalysisEntity>>

    @Query("SELECT * FROM analyses WHERE id = :id LIMIT 1")
    suspend fun getAnalysisById(id: String): AnalysisEntity?

    @Query("SELECT * FROM analyses WHERE mediaType = :mediaType ORDER BY timestamp DESC")
    fun getAnalysesByModality(mediaType: String): Flow<List<AnalysisEntity>>

    @Query("SELECT * FROM analyses WHERE riskScore >= :minRisk ORDER BY timestamp DESC")
    fun getAnalysesByMinRisk(minRisk: Int): Flow<List<AnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity)

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteAnalysisById(id: String)

    @Query("DELETE FROM analyses")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM analyses")
    fun getCount(): Flow<Int>
}
