package com.example.simplewebview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActivityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activity, container, false)
        val rvActivityFeed = view.findViewById<RecyclerView>(R.id.rv_activity_feed)

        val items = listOf(
            ActivityFeedAdapter.ActivityItem("starryskies23", "1d", "Started following you", showButton = true, showPreviewImage = false),
            ActivityFeedAdapter.ActivityItem("nebulanomad", "1d", "Liked your post", showButton = false, showPreviewImage = true),
            ActivityFeedAdapter.ActivityItem("emberecho", "2d", "Liked your comment\nHappy birthday!!", showButton = false, showPreviewImage = false),
            ActivityFeedAdapter.ActivityItem("lunavoyager", "3d", "Saved your post", showButton = false, showPreviewImage = true),
            ActivityFeedAdapter.ActivityItem("shadowlynx", "4d", "Commented on your post: I'm going in september, what about you?", showButton = false, showPreviewImage = false),
            ActivityFeedAdapter.ActivityItem("nebulanomad", "5d", "Shared a post you might like", showButton = false, showPreviewImage = false),
            ActivityFeedAdapter.ActivityItem("lunavoyager", "5d", "Liked your comment\nThis is so adorable!!", showButton = false, showPreviewImage = false)
        )

        rvActivityFeed.layoutManager = LinearLayoutManager(requireContext())
        rvActivityFeed.adapter = ActivityFeedAdapter(items)

        return view
    }
}
