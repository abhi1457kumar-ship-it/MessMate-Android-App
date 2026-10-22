package com.messmate.app.data

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class UserRole(val label: String) {
    STUDENT("Student"),
    ADMIN("Dining team"),
}

data class MealSchedule(val start: LocalTime, val end: LocalTime) {
    val displayTime: String
        get() {
            val startPeriod = start.format(PERIOD_FORMAT)
            val endPeriod = end.format(PERIOD_FORMAT)
            val formattedStart = if (startPeriod == endPeriod) {
                start.format(SHORT_TIME_FORMAT)
            } else {
                start.format(FULL_TIME_FORMAT)
            }
            return "$formattedStart – ${end.format(FULL_TIME_FORMAT)}"
        }

    companion object {
        private val SHORT_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
        private val FULL_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        private val PERIOD_FORMAT = DateTimeFormatter.ofPattern("a", Locale.ENGLISH)
        private val PICKER_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

        fun formatClock(time: LocalTime): String = time.format(PICKER_TIME_FORMAT)
    }
}

enum class MealSlot(val label: String, val defaultSchedule: MealSchedule) {
    BREAKFAST("Breakfast", MealSchedule(LocalTime.of(7, 30), LocalTime.of(9, 30))),
    LUNCH("Lunch", MealSchedule(LocalTime.of(12, 30), LocalTime.of(14, 30))),
    SNACKS("Evening snacks", MealSchedule(LocalTime.of(16, 30), LocalTime.of(17, 30))),
    DINNER("Dinner", MealSchedule(LocalTime.of(19, 30), LocalTime.of(21, 30)));

    val time: String
        get() = defaultSchedule.displayTime
}

data class Meal(
    val name: String,
    val time: String,
    val items: List<String>,
    val note: String = "",
)

object SampleMenus {
    private val breakfasts = listOf(
        listOf("Masala dosa", "Coconut chutney", "Sambar", "Filter coffee"),
        listOf("Aloo paratha", "Curd", "Pickle", "Masala chai"),
        listOf("Poha with peanuts", "Sprout salad", "Banana", "Tea"),
        listOf("Idli & podi", "Tomato chutney", "Sambar", "Filter coffee"),
        listOf("Vegetable upma", "Coconut chutney", "Seasonal fruit", "Tea"),
        listOf("Chole kulche", "Onion salad", "Lassi", "Tea"),
        listOf("Pancakes", "Honey & fruit", "Paneer bhurji", "Coffee"),
    )
    private val lunches = listOf(
        listOf("Paneer butter masala", "Dal tadka", "Jeera rice", "Tandoori roti", "Kachumber"),
        listOf("Rajma masala", "Steamed rice", "Phulka", "Cucumber raita", "Gulab jamun"),
        listOf("Kadai paneer", "Yellow dal", "Veg pulao", "Roti", "Green salad"),
        listOf("Chole masala", "Lemon rice", "Poori", "Curd", "Carrot halwa"),
        listOf("Palak corn", "Dal fry", "Steamed rice", "Roti", "Onion salad"),
        listOf("Veg biryani", "Mirchi ka salan", "Boondi raita", "Papad", "Jalebi"),
        listOf("Matar paneer", "Dal makhani", "Peas pulao", "Naan", "Kheer"),
    )
    private val snacks = listOf(
        listOf("Veg cutlet", "Mint chutney", "Masala chai"),
        listOf("Samosa chaat", "Tamarind chutney", "Ginger tea"),
        listOf("Corn & cheese toast", "Tomato dip", "Cold coffee"),
        listOf("Dhokla", "Green chutney", "Masala chai"),
        listOf("Pav bhaji", "Buttered pav", "Lemon soda"),
        listOf("Aloo tikki", "Chole", "Sweet lassi"),
        listOf("Onion pakora", "Mint chutney", "Ginger tea"),
    )
    private val dinners = listOf(
        listOf("Veg pulao", "Dal makhani", "Boondi raita", "Tandoori roti", "Fruit custard"),
        listOf("Mushroom masala", "Dal palak", "Steamed rice", "Roti", "Seasonal fruit"),
        listOf("Veg kofta curry", "Masoor dal", "Jeera rice", "Butter naan", "Moong halwa"),
        listOf("Pav bhaji", "Vegetable khichdi", "Papad", "Curd", "Ice cream"),
        listOf("Malai kofta", "Dal fry", "Peas pulao", "Roti", "Gajar halwa"),
        listOf("Paneer tikka masala", "Dal makhani", "Rice", "Garlic naan", "Kulfi"),
        listOf("Vegetable handi", "Moong dal", "Steamed rice", "Roti", "Brownie"),
    )

    fun meal(day: DayOfWeek, slot: MealSlot): Meal {
        val index = day.value - 1
        val dishes = when (slot) {
            MealSlot.BREAKFAST -> breakfasts[index]
            MealSlot.LUNCH -> lunches[index]
            MealSlot.SNACKS -> snacks[index]
            MealSlot.DINNER -> dinners[index]
        }
        val name = dishes.first()
        val note = when (slot) {
            MealSlot.BREAKFAST -> "A bright start to your day"
            MealSlot.LUNCH -> "Freshly prepared in the North Mess"
            MealSlot.SNACKS -> "A little pause between classes"
            MealSlot.DINNER -> "End the day around the table"
        }
        return Meal(name = name, time = slot.time, items = dishes, note = note)
    }
}
