/*
 *     This file is part of Kiosk Launcher.
 *
 *     Kiosk Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Kiosk Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Kiosk Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.DevAsh.Launcher.flowerpot.parser

import tech.DevAsh.Launcher.flowerpot.Flowerpot
import tech.DevAsh.Launcher.flowerpot.FlowerpotFormatException
import tech.DevAsh.Launcher.flowerpot.rules.Rule
import tech.DevAsh.Launcher.listWhileNotNull
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class FlowerpotReader(inputStream: InputStream) : BufferedReader(InputStreamReader(inputStream)) {
    private var version: Int? = null

    /**
     * Read the next rule from the stream
     * @return the parsed rule or null if the end of the file has been reached
     */
    fun readRule(): Rule? {
        val line = readLine() ?: return null
        val filter = LineParser.parse(line, version)
        if (filter is Rule.Version) {
            if (version != null) {
                throw FlowerpotFormatException("Version declaration can only appear once")
            }
            if (!Flowerpot.SUPPORTED_VERSIONS.contains(filter.version)) {
                throw FlowerpotFormatException("Unsupported version ${filter.version} (supported are ${Flowerpot.SUPPORTED_VERSIONS.joinToString()})")
            }
            version = filter.version
        }
        return filter
    }

    /**
     * Read all rules contained in the file with None and Version rules already filtered out
     */
    fun readRules(): List<Rule> = listWhileNotNull { readRule() }.filterNot { it is Rule.None || it is Rule.Version }
}