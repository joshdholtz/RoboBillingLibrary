/*   Copyright 2011 Robot Media SL (http://www.robotmedia.net)
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package net.robotmedia.billing.utils;

import android.content.Context;
import android.provider.Settings;
import com.ensolabs.robobilling.logging.Logger;
import net.robotmedia.billing.utils.AESObfuscator.ValidationException;

import java.security.SecureRandom;
import java.util.HashSet;

public class Security {

    private static HashSet<Long> knownNonces = new HashSet<Long>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TAG = Security.class.getSimpleName();

    /**
     * Generates a nonce (a random number used once).
     */
    public static long generateNonce() {
        long nonce = RANDOM.nextLong();
        knownNonces.add(nonce);
        return nonce;
    }

    public static boolean isNonceKnown(long nonce) {
        return knownNonces.contains(nonce);
    }

    public static void removeNonce(long nonce) {
        knownNonces.remove(nonce);
    }

    public static String obfuscate(Context context, byte[] salt, String original) {
        final AESObfuscator obfuscator = getObfuscator(context, salt);
        return obfuscator.obfuscate(original);
    }

    /**
     * This used to return a singleton, but the salt changes between user login/logouts
     * requiring a new Obfuscator to be constructed with the new salt.
     *
     * @param context
     * @param salt
     * @return
     */
    private static AESObfuscator getObfuscator(Context context, byte[] salt) {
        final String installationId = Installation.id(context);
        final String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        final String password = installationId + deviceId + context.getPackageName();
        return new AESObfuscator(salt, password);
    }

    public static String unobfuscate(Context context, byte[] salt, String obfuscated) {
        final AESObfuscator obfuscator = getObfuscator(context, salt);
        try {
            return obfuscator.unobfuscate(obfuscated);
        } catch (ValidationException e) {
            Logger.w(TAG, "Invalid obfuscated data or key");
        }
        return null;
    }

}
