package com.example.campfireapp.presentation.navigation

/**
 * Navigation routes for the Campfire app
 */
object CampfireRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val GROUP_LIST = "group_list"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val CREATE_GROUP = "create_group"
    const val EDIT_GROUP = "edit_group/{groupId}"
    const val PROFILE = "profile"
    const val CHAT = "chat/{groupId}"
    
    fun groupDetail(groupId: String) = "group_detail/$groupId"
    fun editGroup(groupId: String) = "edit_group/$groupId"
    fun chat(groupId: String) = "chat/$groupId"
}
