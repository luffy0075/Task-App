package com.example.util

object Quotes {
    data class DailyQuote(val quote: String, val author: String)

    private val quotesList = listOf(
        DailyQuote("We must all suffer one of two things: the pain of discipline or the pain of regret.", "Jim Rohn"),
        DailyQuote("Self-discipline is the magic power that makes you virtually unstoppable.", "Dan Kennedy"),
        DailyQuote("Dopamine drives desire, but discipline drives destiny.", "Dr. Andrew Huberman"),
        DailyQuote("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
        DailyQuote("Rule your mind or it will rule you.", "Horace"),
        DailyQuote("The secret of freedom lies in educating people, whereas the secret of tyranny is in keeping them ignorant.", "Robespierre"),
        DailyQuote("Small disciplines repeated with consistency every day lead to great achievements.", "John C. Maxwell"),
        DailyQuote("It is not daily increase, but daily decrease. Hack away at the unessential.", "Bruce Lee"),
        DailyQuote("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln"),
        DailyQuote("He who conquers himself is the mightiest warrior.", "Confucius"),
        DailyQuote("Your future is found in your daily routine.", "Mike Murdock"),
        DailyQuote("No man is free who cannot command himself.", "Pythagoras"),
        DailyQuote("The struggle you're in today is developing the strength you need for tomorrow.", "Robert Tew"),
        DailyQuote("The ability to subordinate an impulse to a value is the essence of the proactive person.", "Stephen R. Covey"),
        DailyQuote("Deep work is the superpower of the 21st century.", "Cal Newport"),
        DailyQuote("Success isn't always about greatness. It's about consistency. Consistent hard work leads to success.", "Dwayne Johnson"),
        DailyQuote("Don't count the days, make the days count.", "Muhammad Ali"),
        DailyQuote("First we make our habits, then our habits make us.", "Charles C. Noble"),
        DailyQuote("Action is the foundational key to all success.", "Pablo Picasso"),
        DailyQuote("You will never change your life until you change something you do daily.", "John C. Maxwell"),
        DailyQuote("Mastering others is strength. Mastering yourself is true power.", "Lao Tzu"),
        DailyQuote("The successful warrior is the average person, with laser-like focus.", "Bruce Lee"),
        DailyQuote("Motivation gets you going, but discipline keeps you growing.", "John C. Maxwell"),
        DailyQuote("Focus is a muscle. The more you protect it from cheap dopamine, the stronger it grows.", "Reboot 30"),
        DailyQuote("Continuous effort—not strength or intelligence—is the key to unlocking our potential.", "Winston Churchill"),
        DailyQuote("Comfort is the enemy of achievement.", "Farrah Gray"),
        DailyQuote("Discipline equals freedom.", "Jocko Willink"),
        DailyQuote("Winning begins with the decision to never surrender your self-respect to momentary impulses.", "Reboot 30"),
        DailyQuote("Almost everything will work again if you unplug it for a few minutes, including you.", "Anne Lamott"),
        DailyQuote("You conquered the 30-day forge. Now carry this renewed strength into the rest of your life.", "Reboot 30")
    )

    fun getQuoteForDay(dayNumber: Int): DailyQuote {
        val index = (dayNumber - 1).coerceIn(0, quotesList.size - 1)
        return quotesList[index]
    }
}
