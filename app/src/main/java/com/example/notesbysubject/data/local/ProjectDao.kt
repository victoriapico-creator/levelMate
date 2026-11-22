package com.example.notesbysubject.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY name COLLATE NOCASE, id")
    fun observeProjects(): Flow<List<Project>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeProject(id: Long): Flow<ProjectWithDetails?>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProject(id: Long): ProjectWithDetails?

    @Query("SELECT * FROM photos WHERE projectId IS NULL ORDER BY takenAt DESC, id DESC")
    fun observeGeneralPhotos(): Flow<List<Photo>>

    @Insert suspend fun insertProject(project: Project): Long
    @Update suspend fun updateProject(project: Project): Int
    @Insert suspend fun insertMaterial(material: Material): Long
    @Insert suspend fun insertStep(step: Step): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertPhoto(photo: Photo): Long

    @Query("UPDATE steps SET done = :done WHERE id = :id")
    suspend fun setStepDone(id: Long, done: Boolean)

    @Query("UPDATE photos SET projectId = :projectId WHERE id = :id")
    suspend fun assignPhoto(id: Long, projectId: Long?)

    @Query("DELETE FROM projects WHERE id = :id") suspend fun deleteProject(id: Long)
    @Query("DELETE FROM materials WHERE id = :id") suspend fun deleteMaterial(id: Long)
    @Query("DELETE FROM steps WHERE id = :id") suspend fun deleteStep(id: Long)

    @Query("""
        SELECT * FROM projects
        WHERE deadline IS NOT NULL AND deadline <= :today
        AND (NOT EXISTS (SELECT 1 FROM steps WHERE projectId = projects.id)
             OR EXISTS (SELECT 1 FROM steps WHERE projectId = projects.id AND done = 0))
        ORDER BY deadline, name COLLATE NOCASE
    """)
    suspend fun dueProjects(today: String): List<Project>
}
