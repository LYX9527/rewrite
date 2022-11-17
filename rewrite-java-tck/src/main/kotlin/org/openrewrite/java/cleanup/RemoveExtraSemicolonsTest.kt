/*
 * Copyright 2021 the original author or authors.
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
package org.openrewrite.java.cleanup

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openrewrite.Issue
import org.openrewrite.java.Assertions.java
import org.openrewrite.java.JavaRecipeTest
import org.openrewrite.java.tree.J
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

@Suppress("UnnecessarySemicolon")
interface RemoveExtraSemicolonsTest : RewriteTest, JavaRecipeTest {

    override fun defaults(spec: RecipeSpec) {
        spec.recipe(RemoveExtraSemicolons())
    }

    @Issue("https://github.com/openrewrite/rewrite/issues/1587")
    @Test
    fun enumSemicolons() = rewriteRun(
       java("""
            public enum FRUITS {
                BANANA,
                APPLE;
            }
        """,
        """
            public enum FRUITS {
                BANANA,
                APPLE
            }
        """)
    )

    @Issue("https://github.com/openrewrite/rewrite/issues/1587")
    @Test
    fun enumSemicolonsWithOtherStatements() = rewriteRun(
        java("""
            public enum FRUITS {
                BANANA,
                APPLE;
                
                void hiFruit() {}
            }
        """)
    )

    @Test
    fun emptyBlockStatements() = rewriteRun(
        java("""
            class Test {
                void test() {
                    ;
                }
            }
        """,
        """
            class Test {
                void test() {
                }
            }
        """)
    )

    @Suppress("EmptyTryBlock")
    @Test
    fun tryWithResources() = rewriteRun(
        java("""
            import java.io.*;
            class Test {
                void test() {
                    try (ByteArrayInputStream b = new ByteArrayInputStream(new byte[10]);
                          Reader r = new InputStreamReader(b);) {
                    }
                }
            }
        """,
        """
            import java.io.*;
            class Test {
                void test() {
                    try (ByteArrayInputStream b = new ByteArrayInputStream(new byte[10]);
                          Reader r = new InputStreamReader(b)) {
                    }
                }
            }
        """) { s -> s.afterRecipe { cu ->
                val jTry = (((cu as J.CompilationUnit).classes[0].body.statements[0] as J.MethodDeclaration)
                    .body!!.statements[0] as J.Try)
                assertThat(jTry.resources!![0].isTerminatedWithSemicolon).isTrue
                assertThat(jTry.resources!![1].isTerminatedWithSemicolon).isFalse()
            }
        }
    )
}
