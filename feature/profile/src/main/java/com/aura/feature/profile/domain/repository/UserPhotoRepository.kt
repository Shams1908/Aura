package com.aura.feature.profile.domain.repository

import com.aura.feature.profile.domain.model.UserPhoto
import kotlinx.coroutines.flow.Flow

/**
 * Interface contract defining CRUD operations for user photos.
 */
@Deprecated(
    message = "Aura no longer uses profile photos. Migrate workflows to use OutfitSession and SessionManager instead.",
    level = DeprecationLevel.WARNING
)
interface UserPhotoRepository {
    
    /**
     * Emits the list of user photos.
     */
    fun getPhotos(): Flow<Result<List<UserPhoto>>>
    
    /**
     * Uploads/Registers a new user photo URI.
     */
    fun uploadPhoto(uri: String, name: String): Flow<Result<UserPhoto>>
    
    /**
     * Sets a specific photo as default and updates all others.
     */
    fun setDefaultPhoto(photoId: String): Flow<Result<Unit>>
    
    /**
     * Renames an existing photo.
     */
    fun renamePhoto(photoId: String, newName: String): Flow<Result<Unit>>
    
    /**
     * Deletes a user photo.
     */
    fun deletePhoto(photoId: String): Flow<Result<Unit>>
}
