package ch.deletescape.lawnchair.flowerpot.parser

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class ChinapotReader(inputStream: InputStream) : BufferedReader(InputStreamReader(inputStream)) {

    /**
     * Read the next rule from the stream
     * @return the parsed rule or null if the end of the file has been reached
     */
    fun readRules(): ArrayList<String>? {
        val pkgs = ArrayList<String>()
        forEachLine {
            if (!it.isBlank()) {
                pkgs.add(it)
            }
        }
        return pkgs
    }

}