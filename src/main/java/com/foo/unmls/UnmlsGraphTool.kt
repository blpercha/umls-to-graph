package com.foo.unmls

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int

object UnmlsGraphTool {
    @JvmStatic
    fun main(args: Array<String>) {
        Cli().main(args)
    }
}

private class Cli : CliktCommand() {
    init {
        subcommands(
            Foo(),
            Bar()
        )
    }

    override fun run() {
        // no op
    }
}

private class Foo : CliktCommand() {
    private val widgetSize: Int by option().int().required()

    override fun run() {
        println("Size $widgetSize widget")
    }
}

private class Bar : CliktCommand() {
    override fun run() {
        println("Hello world, again")
    }
}
