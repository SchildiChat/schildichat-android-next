package chat.schildi.unifiedpush

import org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver
import org.unifiedpush.android.embedded_fcm_distributor.Gateway

class FossFcmDistributor: EmbeddedDistributorReceiver() {

    override val gateway = object : Gateway {
        override val vapid = "BHX_kWKtwJNHO0oQrbyvyDpB0_FIs5ZnzlOZ5lwBtocEE55GJkcBfuPJfkEt1zjBseFdG_eGJrrmiwSm8N_vIUA"

        override fun getEndpoint(token: String): String {
            return "https://up.spiritcroc.de/wpfcm?t=$token"
        }
    }

}
