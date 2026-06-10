package com.cs346.intellical.ui.screens.about

import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {
    
    val appName = "IntelliCal"
    val version = "Version 1.0.0"
    val tagline = "Your Intelligent Calorie Companion"
    val teamCredit = "Made by Team IntelliCal"
    val copyright = "2026 IntelliCal. All rights reserved."
    
    val descriptionParagraphs = listOf(
        "IntelliCal is a revolutionary nutrition tracking app designed to make healthy eating effortless and enjoyable. We believe that understanding what you eat shouldn't be complicated or time-consuming.",
        "Using advanced AI-powered image recognition, IntelliCal allows you to simply snap a photo of your meal, and our intelligent system will automatically identify the foods on your plate, estimate portion sizes, and calculate the nutritional content including calories, macronutrients, and more.",
        "But IntelliCal is more than just a calorie counter. We've gamified your health journey with our unique Forest feature. As you consistently track your meals and make progress toward your goals, you'll nurture and grow a virtual forest. Each tree represents your dedication and healthy choices, creating a beautiful visual representation of your wellness journey.",
        "Whether you're looking to lose weight, gain muscle, or simply maintain a balanced diet, IntelliCal provides personalized recommendations based on your goals, activity level, and dietary preferences. Our smart algorithms adapt to your eating patterns and provide insights to help you make better choices every day."
    )
    
    val features = listOf(
        "AI-powered food recognition from photos",
        "Detailed nutritional breakdown and tracking",
        "Personalized calorie and macro goals",
        "Gamified forest growth system",
        "Progress tracking and insights",
        "Smart reminders and meal logging"
    )
}
