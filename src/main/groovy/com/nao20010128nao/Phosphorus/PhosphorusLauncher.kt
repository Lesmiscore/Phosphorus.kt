package com.nao20010128nao.Phosphorus

import com.nao20010128nao.Phosphorus.phar.PharParser
import com.nao20010128nao.Phosphorus.phar.events.PharParserEvent
import com.nao20010128nao.Phosphorus.phar.events.RawFileEvent
import com.nao20010128nao.Phosphorus.phar.events.SignatureValidatedEvent
import com.nao20010128nao.Phosphorus.phar.events.StubEvent
import joptsimple.OptionParser
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import java.io.ByteArrayInputStream
import java.io.File
import java.util.regex.Pattern
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

object PhosphorusLauncher {
    @JvmStatic
    fun main(args: Array<String>) {


        val opt = OptionParser()
        opt.accepts("input").withRequiredArg()
        opt.accepts("output").withOptionalArg()
        opt.accepts("stub").withOptionalArg()
        opt.accepts("check")
        val result = opt.parse(*args)

        val input: File
        val output: File
        val stub: File?

        if (!result.has("input")) {
            println("input argument is required.")
            println("Usage:")
            println("--input=(filename) - Input file name (full path) of the PHAR file. (required)")
            println("--output=(dirname) - Output directory to save extracted result (default is input+\"_extracted\")")
            println("--stub=(filename) - Relative path from output to save stub file (default is null, and never save)")
            println("--check - Check the input from command line, and never save any file")
            System.exit(1)
            return

        } else {
            input = File(result.valueOf("input").toString()).absoluteFile
            println("IN: $input")
        }

        output = if (!result.has("output")) {
            File(input.parentFile, DefaultGroovyMethods.last(input.absolutePath.split(Pattern.quote(File.separator).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) + "_extracted")
        } else {
            File(result.valueOf("output").toString()).absoluteFile
        }

        println("OUT: $output")

        if (!result.has("stub")) {
            stub = null//do not export stub
        } else {
            stub = File(output, result.valueOf("stub").toString()).absoluteFile
            println("STUB: $stub")
        }


        if (result.has("check")) {
            System.exit(0)
        }


        println("Parsing PHAR file...")

        val parser = PharParser(input)

        println("Extracting...")

        var extractedStub: Boolean = false
        var extractedCount: Int = 0

        parser.startReading(object : PharParser.OnEventListener {
            override fun onEvent(event: PharParserEvent) {
                if ((event is StubEvent && stub != null && !extractedStub)) {
                    println("Extracting stub")
                    stub.parentFile!!.mkdirs()
                    stub.writeBytes(event.stub)
                    extractedStub = (true)
                }

                if (event is RawFileEvent) {
                    val decompress = a@{ ev: RawFileEvent ->
                        val manifest = ev.manifest
                        val raw = ev.raw
                        return@a when {
                            manifest.isNotCompressed -> raw
                            manifest.isZlibCompressed -> InflaterInputStream(ByteArrayInputStream(raw), Inflater(true)).readBytes()
                            manifest.isBzipCompressed -> BZip2CompressorInputStream(ByteArrayInputStream(raw), true).readBytes()
                            else -> raw
                        }
                    }
                    println("Extract: " + event.manifest.fileNameString)
                    val dest = File(output, event.manifest.fileNameString)
                    dest.parentFile.mkdirs()
                    dest.writeBytes(decompress(event))
                    extractedCount++
                }

                if (event is SignatureValidatedEvent) {
                    println("Signature check: " + if (event.correct) "OK" else "NG")
                }
            }
        })

        println("Extracted $extractedCount files" + if (extractedStub) " with a stub" else "")
        println("Done")
    }
}
