package chat.schildi.matrixsdk

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val ROOM_ACCOUNT_DATA_SPACE_ORDER = "org.matrix.msc3230.space_order"

object SpaceOrderSerializer {
    private val coder = Json { ignoreUnknownKeys = true }
    fun deserialize(data: String): Result<SpaceOrder> {
        return runCatching { coder.decodeFromString(SpaceOrder.serializer(), data) }
    }
    fun deserializeContent(data: String): Result<SpaceOrder.Content> {
        return runCatching { coder.decodeFromString(SpaceOrder.Content.serializer(), data) }
    }
    fun serialize(data: SpaceOrder): String {
        return coder.encodeToString(SpaceOrder.serializer(), data)
    }
}
@Serializable
data class SpaceOrder(
    val content: Content,
) {
    @Serializable
    data class Content(
        val order: String?,
    )
}
