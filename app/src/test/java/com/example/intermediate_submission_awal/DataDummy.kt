package com.example.intermediate_submission_awal

import com.example.intermediate_submission_awal.data.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0 until 100) {
            val story = ListStoryItem(
                id = i.toString(),
                name = "User + $i",
                description = "Description $i",
                photoUrl = "https://example.com/photo$i",
                createdAt = "2024-12-24"
            )
            items.add(story)
        }
        return items
    }
}