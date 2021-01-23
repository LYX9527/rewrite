/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openrewrite.RecipeTest

interface ChangeMethodNameTest : RecipeTest {
    companion object {
        private const val b: String = """
            package com.abc;
            class B {
               public void singleArg(String s) {}
               public void arrArg(String[] s) {}
               public void varargArg(String... s) {}
               public static void static1(String s) {}
               public static void static2(String s) {}
            }
        """
    }

    @Test
    fun changeMethodNameForMethodWithSingleArgDeclarative(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(b),
        recipe = ChangeMethodName("com.abc.B singleArg(String)", "bar"),
        before = """
            package com.abc;
            class A {
               public void test() {
                   new B().singleArg("boo");
               }
            }
        """,
        after = """
            package com.abc;
            class A {
               public void test() {
                   new B().bar("boo");
               }
            }
        """
    )

    @Test
    fun changeMethodNameForMethodWithSingleArg(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(b),
        recipe = ChangeMethodName("com.abc.B singleArg(String)", "bar"),
        before = """
            package com.abc;
            class A {
               public void test() {
                   new B().singleArg("boo");
               }
            }
        """,
        after = """
            package com.abc;
            class A {
               public void test() {
                   new B().bar("boo");
               }
            }
        """
    )

    @Test
    fun changeMethodNameForMethodWithArrayArg(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(b),
        recipe = ChangeMethodName("com.abc.B arrArg(String[])", "bar"),
        before = """
            package com.abc;
            class A {
               public void test() {
                   new B().arrArg(new String[] {"boo"});
               }
            }
        """,
        after = """
            package com.abc;
            class A {
               public void test() {
                   new B().bar(new String[] {"boo"});
               }
            }
        """
    )

    @Test
    fun changeMethodNameForMethodWithVarargArg(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(b),
        recipe = ChangeMethodName("com.abc.B varargArg(String...)", "bar"),
        before = """
            package com.abc;
            class A {
               public void test() {
                   new B().varargArg("boo", "again");
               }
            }
        """,
        after = """
            package com.abc;
            class A {
               public void test() {
                   new B().bar("boo", "again");
               }
            }
        """
    )

    @Test
    fun changeMethodNameWhenMatchingAgainstMethodWithNameThatIsAnAspectjToken(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(
            """
                package com.abc;
                class B {
                   public void error() {}
                   public void foo() {}
                }
            """
        ),
        recipe = ChangeMethodName("com.abc.B error()", "foo"),
        before = """
            package com.abc;
            class A {
               public void test() {
                   new B().error();
               }
            }
        """,
        after = """
            package com.abc;
            class A {
               public void test() {
                   new B().foo();
               }
            }
        """
    )

    @Test
    fun changeMethodDeclarationForMethodWithSingleArg(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(b),
        recipe = ChangeMethodName("com.abc.A foo(String)", "bar"),
        before = """
            package com.abc;
            class A {
               public void foo(String s) {
               }
            }
        """,
        after = """
            package com.abc;
            class A {
               public void bar(String s) {
               }
            }
        """
    )

    @Test
    fun changeStaticMethodTest(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(b),
        recipe = ChangeMethodName("com.abc.B static1(String)", "static2"),
        before = """
            package com.abc;
            class A {
               public void test() {
                   B.static1("boo");
               }
            }
        """,
        after = """
            package com.abc;
            class A {
               public void test() {
                   B.static2("boo");
               }
            }
        """
    )

    @Test
    fun changeStaticImportTest(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(b),
        recipe = ChangeMethodName("com.abc.B static1(String)", "static2"),
        before = """
            package com.abc;
            import static com.abc.B.static1;
            class A {
               public void test() {
                   static1("boo");
               }
            }
        """,
        after = """
            package com.abc;
            import static com.abc.B.static2;
            class A {
               public void test() {
                   static2("boo");
               }
            }
        """
    )

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun checkValidation() {
        var recipe = ChangeMethodName(null, null)
        var valid = recipe.validate()
        assertThat(valid.isValid).isFalse()
        assertThat(valid.failures()).hasSize(2)
        assertThat(valid.failures()[0].property).isEqualTo("methodPattern")
        assertThat(valid.failures()[1].property).isEqualTo("newMethodName")

        recipe = ChangeMethodName(null, "hello")
        valid = recipe.validate()
        assertThat(valid.isValid).isFalse()
        assertThat(valid.failures()).hasSize(1)
        assertThat(valid.failures()[0].property).isEqualTo("methodPattern")

        recipe = ChangeMethodName("java.util.String emptyString(..)", null)
        valid = recipe.validate()
        assertThat(valid.isValid).isFalse()
        assertThat(valid.failures()).hasSize(1)
        assertThat(valid.failures()[0].property).isEqualTo("newMethodName")
    }

}
