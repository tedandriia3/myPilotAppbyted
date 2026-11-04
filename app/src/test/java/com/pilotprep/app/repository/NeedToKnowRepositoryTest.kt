package com.pilotprep.app.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.repository.NeedToKnowRepository
import com.pilotprep.app.util.ErrorHandler
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NeedToKnowRepositoryTest {

    @Test
    fun getAllContents_whenFirestoreThrows_returnsEmptyList() = runTest(StandardTestDispatcher()) {
        val db = mockk<FirebaseFirestore>()
        val collection = mockk<CollectionReference>()
        every { db.collection("need_to_know") } returns collection
        every { collection.get() } throws RuntimeException("boom")

        val errorHandler = mockk<ErrorHandler>(relaxed = true)
        val repo = NeedToKnowRepository(db, errorHandler)
        val result = repo.getAllContents()
        assertTrue(result.isEmpty())
    }
}

