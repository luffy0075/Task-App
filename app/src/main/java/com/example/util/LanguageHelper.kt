package com.example.util

object LanguageHelper {
    private val translationsEn = mapOf(
        "nav_home" to "Home",
        "nav_tasks" to "My Tasks",
        "nav_achievements" to "Milestones",
        "nav_challenge" to "Challenges",
        "nav_analytics" to "Analytics",
        "nav_profile" to "Profile",
        "nav_settings" to "Settings",
        
        "today_tasks" to "Today's Tasks",
        "daily_habits" to "Daily Habits",
        "progress_overview" to "Progress Overview",
        "weekly_stats" to "Weekly Statistics",
        "quick_actions" to "Quick Actions",
        
        "day_of_30" to "Day %d of 30",
        "overall_challenge" to "30-Day Completion",
        "today_completion" to "Today's Completion",
        "current_streak" to "Current Streak",
        "longest_streak" to "Longest Streak",
        "active_habits" to "Active Habits",
        "completed_tasks" to "Completed",
        "add_task" to "Add New Task",
        "edit_task" to "Edit Task",
        "delete_task" to "Delete Task",
        
        "check_in" to "Daily Check-in",
        "screen_time" to "Screen Time",
        "study_time" to "Study Duration",
        "exercise_time" to "Exercise",
        "sleep_time" to "Sleep",
        "mood" to "Mood",
        "productivity_rating" to "Productivity Rating",
        "journal_notes" to "Personal Notes / Reflection",
        "save" to "Save",
        "cancel" to "Cancel",
        "close" to "Close",
        
        "phase_1" to "Phase 1: Control Yourself",
        "phase_2" to "Phase 2: Build Discipline",
        "phase_3" to "Phase 3: Deep Focus",
        "phase_4" to "Phase 4: New Lifestyle"
    )

    private val translationsBn = mapOf(
        "nav_home" to "হোম",
        "nav_tasks" to "আমার কাজ",
        "nav_achievements" to "মাইলস্টোন",
        "nav_challenge" to "চ্যালেঞ্জসমূহ",
        "nav_analytics" to "পরিসংখ্যান",
        "nav_profile" to "প্রোফাইল",
        "nav_settings" to "সেটিংস",
        
        "today_tasks" to "আজকের কাজসমূহ",
        "daily_habits" to "দৈনিক অভ্যাস",
        "progress_overview" to "অগ্রগতির সারসংক্ষেপ",
        "weekly_stats" to "সাপ্তাহিক পরিসংখ্যান",
        "quick_actions" to "দ্রুত কার্যক্রম",
        
        "day_of_30" to "৩০ দিনের মধ্যে %dতম দিন",
        "overall_challenge" to "৩০ দিনের সম্পন্নতা",
        "today_completion" to "আজকের সম্পন্নতা",
        "current_streak" to "বর্তমান ধারাবাহিকতা",
        "longest_streak" to "সর্বোচ্চ ধারাবাহিকতা",
        "active_habits" to "সক্রিয় অভ্যাস",
        "completed_tasks" to "সম্পন্ন কাজ",
        "add_task" to "নতুন কাজ যুক্ত করুন",
        "edit_task" to "কাজ সম্পাদনা",
        "delete_task" to "কাজ মুছে ফেলুন",
        
        "check_in" to "দৈনিক চেক-ইন",
        "screen_time" to "স্ক্রিন টাইম",
        "study_time" to "পড়াশোনার সময়",
        "exercise_time" to "ব্যায়াম",
        "sleep_time" to "ঘুম",
        "mood" to "মানসিক অবস্থা",
        "productivity_rating" to "উৎপাদনশীলতার রেটিং",
        "journal_notes" to "ব্যক্তিগত নোট / প্রতিফলন",
        "save" to "সংরক্ষণ",
        "cancel" to "বাতিল",
        "close" to "বন্ধ",
        
        "phase_1" to "ধাপ ১: আত্মনিয়ন্ত্রণ",
        "phase_2" to "ধাপ ২: শৃঙ্খলা গঠন",
        "phase_3" to "ধাপ ৩: গভীর মনোযোগ",
        "phase_4" to "ধাপ ৪: নতুন জীবনধারা"
    )

    fun getString(key: String, language: String = "EN", vararg args: Any): String {
        val map = if (language.equals("BN", ignoreCase = true)) translationsBn else translationsEn
        val pattern = map[key] ?: translationsEn[key] ?: key
        return if (args.isNotEmpty()) {
            try {
                String.format(pattern, *args)
            } catch (_: Exception) {
                pattern
            }
        } else {
            pattern
        }
    }
}
