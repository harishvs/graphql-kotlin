package com.expedia.graphql.generator.types

import com.expedia.graphql.annotations.GraphQLDescription
import com.expedia.graphql.annotations.GraphQLID
import com.expedia.graphql.annotations.GraphQLName
import com.expedia.graphql.exceptions.InvalidInputFieldTypeException
import com.expedia.graphql.test.utils.SimpleDirective
import graphql.schema.GraphQLNonNull
import org.junit.jupiter.api.Test
import kotlin.reflect.full.findParameterByName
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class ArgumentBuilderTest : TypeTestHelper() {

    private lateinit var builder: ArgumentBuilder

    override fun beforeTest() {
        builder = ArgumentBuilder(generator)
    }

    internal interface MyInterface {
        val id: String
    }

    internal class ArgumentTestClass {
        fun description(@GraphQLDescription("Argument description") input: String) = input

        fun directive(@SimpleDirective input: String) = input

        fun changeName(@GraphQLName("newName") input: String) = input

        fun id(@GraphQLID idArg: Int) = "Your id is $idArg"

        fun interfaceArg(input: MyInterface) = input.id
    }

    @Test
    fun `Description is set on arguments`() {
        val kParameter = ArgumentTestClass::description.findParameterByName("input")
        assertNotNull(kParameter)
        val result = builder.argument(kParameter)

        assertEquals("String", (result.type as? GraphQLNonNull)?.wrappedType?.name)
        assertEquals("Argument description", result.description)
    }

    @Test
    fun `Directives are included on arguments`() {
        val kParameter = ArgumentTestClass::directive.findParameterByName("input")
        assertNotNull(kParameter)
        val result = builder.argument(kParameter)

        assertEquals("String", (result.type as? GraphQLNonNull)?.wrappedType?.name)
        assertEquals(1, result.directives.size)
        assertEquals("simpleDirective", result.directives.firstOrNull()?.name)
    }

    @Test
    fun `Argument names can be changed with @GraphQLName`() {
        val kParameter = ArgumentTestClass::changeName.findParameterByName("input")
        assertNotNull(kParameter)
        val result = builder.argument(kParameter)

        assertEquals("String", (result.type as? GraphQLNonNull)?.wrappedType?.name)
        assertEquals("newName", result.name)
    }

    @Test
    fun `ID argument type is valid`() {
        val kParameter = ArgumentTestClass::id.findParameterByName("idArg")
        assertNotNull(kParameter)
        val result = builder.argument(kParameter)

        assertEquals(expected = "idArg", actual = result.name)
        assertEquals("ID", (result.type as? GraphQLNonNull)?.wrappedType?.name)
    }

    @Test
    fun `Interface argument type throws exception`() {
        val kParameter = ArgumentTestClass::interfaceArg.findParameterByName("input")
        assertNotNull(kParameter)

        assertFailsWith(InvalidInputFieldTypeException::class) {
            builder.argument(kParameter)
        }
    }
}
