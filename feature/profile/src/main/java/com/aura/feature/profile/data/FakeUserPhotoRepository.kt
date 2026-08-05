package com.aura.feature.profile.data

import com.aura.feature.profile.domain.model.PhotoQuality
import com.aura.feature.profile.domain.model.UserPhoto
import com.aura.feature.profile.domain.repository.UserPhotoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Fake repository managing mock state in memory.
 */
@Singleton
class FakeUserPhotoRepository @Inject constructor() : UserPhotoRepository {

    private val photosState = MutableStateFlow<List<UserPhoto>>(
        listOf(
            UserPhoto(
                id = "1",
                uri = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=300",
                name = "studio_headshot.jpg",
                dateAdded = "2026-08-01",
                isDefault = true,
                quality = PhotoQuality(
                    poseFrontFacing = true,
                    lightingGood = true,
                    backgroundClean = true,
                    resolutionHigh = true
                )
            ),
            UserPhoto(
                id = "2",
                uri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=300",
                name = "outdoor_portrait.png",
                dateAdded = "2026-08-03",
                isDefault = false,
                quality = PhotoQuality(
                    poseFrontFacing = true,
                    lightingGood = true,
                    backgroundClean = false,
                    resolutionHigh = true
                )
            )
        )
    )

    override fun getPhotos(): Flow<Result<List<UserPhoto>>> {
        return photosState.map { Result.success(it) }
    }

    override fun uploadPhoto(uri: String, name: String): Flow<Result<UserPhoto>> = flow {
        delay(1000) // Simulating upload time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Mock checking quality
        val mockQuality = PhotoQuality(
            poseFrontFacing = true,
            lightingGood = true,
            backgroundClean = true,
            resolutionHigh = true
        )
        
        val newPhoto = UserPhoto(
            id = UUID.randomUUID().toString(),
            uri = uri,
            name = name,
            dateAdded = dateFormat.format(Date()),
            isDefault = false,
            quality = mockQuality
        )
        
        val currentList = photosState.value.toMutableList()
        currentList.add(newPhoto)
        photosState.value = currentList
        
        emit(Result.success(newPhoto))
    }

    override fun setDefaultPhoto(photoId: String): Flow<Result<Unit>> = flow {
        delay(300)
        val currentList = photosState.value.map {
            it.copy(isDefault = it.id == photoId)
        }
        photosState.value = currentList
        emit(Result.success(Unit))
    }

    override fun renamePhoto(photoId: String, newName: String): Flow<Result<Unit>> = flow {
        delay(300)
        val currentList = photosState.value.map {
            if (it.id == photoId) it.copy(name = newName) else it
        }
        photosState.value = currentList
        emit(Result.success(Unit))
    }

    override fun deletePhoto(photoId: String): Flow<Result<Unit>> = flow {
        delay(300)
        val currentList = photosState.value.filter { it.id != photoId }
        
        // If we deleted the default, automatically assign the first photo as default
        val hasDefault = currentList.any { it.isDefault }
        val updatedList = if (!hasDefault && currentList.isNotEmpty()) {
            currentList.mapIndexed { index, photo ->
                if (index == 0) photo.copy(isDefault = true) else photo
            }
        } else {
            currentList
        }
        
        photosState.value = updatedList
        emit(Result.success(Unit))
    }
}
