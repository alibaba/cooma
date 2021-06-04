package com.alibaba.microkernel

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

class SetComparatorCautionTest {

    @Test
    fun setComparator_ElementDisappear_whenComparatorIsEqual() {
        val set = ConcurrentSkipListSet(
                Comparator.comparing { s: String -> s[0] } // comparing by first char
        )
        set.addAll(listOf("Dog", "Disappear!!", "Cat", "Disappear!!!"))

        assertThat(set).containsExactlyInAnyOrder("Dog", "Cat")
    }

    @Test
    fun setComparator_thenComparing_identityHashCode_NotDisappear() {
        val set = ConcurrentSkipListSet(
                Comparator.comparing { s: String -> s[0] }  // comparing by first char
                        .thenComparing<Int>(System::identityHashCode) // then identityHashCode
        )
        val elements = listOf("Dog", "Disappear!!", "Cat", "Disappear!!!")
        set.addAll(elements)

        assertThat(set).containsExactlyInAnyOrderElementsOf(elements)
    }
}
