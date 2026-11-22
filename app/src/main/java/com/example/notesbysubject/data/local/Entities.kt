package com.example.notesbysubject.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val deadline: String? = null
)

@Entity(
    tableName = "materials",
    foreignKeys = [ForeignKey(
        entity = Project::class, parentColumns = ["id"], childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val quantity: String
)

@Entity(
    tableName = "steps",
    foreignKeys = [ForeignKey(
        entity = Project::class, parentColumns = ["id"], childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["projectId", "position"])]
)
data class Step(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val text: String,
    val done: Boolean = false,
    val position: Int = 0
)

@Entity(
    tableName = "photos",
    foreignKeys = [ForeignKey(
        entity = Project::class, parentColumns = ["id"], childColumns = ["projectId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("projectId"), Index(value = ["path"], unique = true)]
)
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val path: String,
    val takenAt: Long = System.currentTimeMillis()
)

data class ProjectWithDetails(
    @Embedded val project: Project,
    @Relation(parentColumn = "id", entityColumn = "projectId") val materials: List<Material>,
    @Relation(parentColumn = "id", entityColumn = "projectId") val steps: List<Step>,
    @Relation(parentColumn = "id", entityColumn = "projectId") val photos: List<Photo>
)
