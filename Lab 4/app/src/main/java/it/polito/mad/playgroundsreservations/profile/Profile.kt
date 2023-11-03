package it.polito.mad.playgroundsreservations.profile

enum class Gender {
    MALE, FEMALE, OTHER
}

data class Profile(
    var name: String?,
    var nickname: String?,
    var bio: String?,
    var age: Int?,
    var gender: Gender?,
    var phone: String?,
    var location: String?,
    var rating: Float?,
    var userProfileImageUriString: String?
)