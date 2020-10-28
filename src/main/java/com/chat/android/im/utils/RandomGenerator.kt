package com.chat.android.im.utils

/**
 * Created by Ryan on 2020/8/25.
 */
class RandomGenerator {

    val UNMISTAKABLE_CHARS: String = "23456789ABCDEFGHJKLMNPQRSTWXYZabcdefghijkmnopqrstuvwxyz"
    val BASE64_CHARS: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789-_"


    /**
     * @name Random.hexString
     * @summary Return a random string of `n` hexadecimal digits.
     * @locus Anywhere
     * @param {Number} n Length of the string
     */
    fun hexString(digits: Int = 17): String {
        return this._randomString(digits, UNMISTAKABLE_CHARS)
    }

    fun _randomString(charsCount: Int, alphabet: String): String {
        var result = ""
        for (i in 0 until charsCount) {
            result += this._choice(alphabet)
        }
        return result
    }

    /**
     * @name Random.id
     * @summary Return a unique identifier, such as `"Jjwjg6gouWLXhMGKW"`, that is
     * likely to be unique in the whole world.
     * @locus Anywhere
     * @param {Number} [n] Optional length of the identifier in characters
     *   (defaults to 17)
     */
//    fun id(charsCount) {
//        // 17 characters is around 96 bits of entropy, which is the amount of
//        // state in the Alea PRNG.
//        if (charsCount === undefined) {
//            charsCount = 17;
//        }
//
//        return this._randomString(charsCount, UNMISTAKABLE_CHARS);
//    }

    /**
     * @name Random.secret
     * @summary Return a random string of printable characters with 6 bits of
     * entropy per character. Use `Random.secret` for security-critical secrets
     * that are intended for machine, rather than human, consumption.
     * @locus Anywhere
     * @param {Number} [n] Optional length of the secret string (defaults to 43
     *   characters, or 256 bits of entropy)
     */
//    fun secret(charsCount) {
//        // Default to 256 bits of entropy, or 43 characters at 6 bits per
//        // character.
//        if (charsCount === undefined) {
//            charsCount = 43;
//        }
//
//        return this._randomString(charsCount, BASE64_CHARS);
//    }

    /**
     * @name Random.choice
     * @summary Return a random element of the given array or string.
     * @locus Anywhere
     * @param {Array|String} arrayOrString Array or string to choose from
     */
    fun _choice(arrayOrString: String): String {
        var index = Math.floor(Math.random() * arrayOrString.length).toInt()
        return arrayOrString.get(index).toString()
    }

}