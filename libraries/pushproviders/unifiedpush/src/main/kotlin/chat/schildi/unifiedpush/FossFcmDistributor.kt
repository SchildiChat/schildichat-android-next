package chat.schildi.unifiedpush

import android.content.Context
import org.unifiedpush.android.foss_embedded_fcm_distributor.EmbeddedDistributorReceiver

class FossFcmDistributor: EmbeddedDistributorReceiver() {

    override val googleProjectNumber = "326900467720" // This value comes from the google-services.json

    override fun getEndpoint(context: Context, token: String, instance: String): String {
        // This returns the endpoint of your FCM Rewrite-Proxy
        return "https://up.schildi.chat/FCM?v2&instance=$instance&token=$token"
    }
}
