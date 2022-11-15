package uk.co.sentinelweb.cuer.app.ext

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.domain.ext.domainClassDiscriminator
import uk.co.sentinelweb.cuer.domain.ext.domainSerializersModule

// fixme make a new serializer for onboarding?
fun OnboardingContract.Config.serialise() = domainJsonSerializer.encodeToString(
    OnboardingContract.Config.serializer(), this
)

fun deserialiseOnboarding(input: String) = domainJsonSerializer.decodeFromString(
    OnboardingContract.Config.serializer(), input
)

val domainJsonSerializer = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    classDiscriminator = domainClassDiscriminator
    serializersModule = domainSerializersModule.plus(SerializersModule {
        contextual(OnboardingContract.Config::class, OnboardingContract.Config.serializer())
    })
}

