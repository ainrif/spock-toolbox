/*
 * Copyright 2014-2016 Ainrif <support@ainrif.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ainrif.gears.spock_device.internal

import org.unitils.reflectionassert.comparator.Comparator
import org.unitils.reflectionassert.difference.Difference
import org.unitils.reflectionassert.report.impl.DefaultDifferenceView

/**
 * Matcher provides reflection comparator for POJO types.
 * More convenient way to use it via
 * {@link com.ainrif.gears.spock_device.Tricorder#reflects(java.lang.Object, java.lang.Object)}
 * <p>
 * This matcher provides implementation of asBoolean method so can be used in any assertion sentences
 * or `then:` & `expect:` stanzas of Spock Framework
 */
class ReflectsMatcher {
    private final def actual
    private final def expected
    private List<Class<? extends Comparator>> modes = []
    private List<Comparator> comparators = []
    private List<String> excludedFields = []

    private List<String> excludedReport
    private List<String> diffReport

    ReflectsMatcher(actual, expected) {
        this.actual = actual
        this.expected = expected
    }

    /**
     * Set list of comparison modes. Replaces all added before.
     * <p>
     * Any class of comparator will be instantiates with default constructor and cached as singleton.
     * Classes from package *.comparator are already pre-cached
     * <p>
     * Example:
     * <pre>
     * Tricorder.reflects(obj1, obj2)
     *   .modes(IGNORE_DEFAULTS, IGNORE_TIME_DIFF)
     * </pre>
     */
    public ReflectsMatcher modes(Class<? extends Comparator>... modes) {
        this.modes = modes as List
        this
    }

    /**
     * Add mode to the list of comparison modes.
     * <p>
     * Any class of comparator will be instantiates with default constructor and cached as singleton.
     * Classes from package *.comparator are already pre-cached
     * <p>
     * Example:
     * <pre>
     * Tricorder.reflects(obj1, obj2)
     *   .mode STRICT_ORDER
     * </pre>
     */
    public ReflectsMatcher mode(Class<? extends Comparator> mode) {
        this.modes += mode
        this
    }

    /**
     * Set list of concrete comparators. Replaces all added before.
     * <p>
     * Some modes can be instantiated with especial params f.e. {@link com.ainrif.gears.spock_device.comparator.DOUBLE_SCALE}.
     * By default it's used with scale 1e-14 but it can be changed via instance of comparator:
     * <pre>
     * Tricorder.reflects(new Double(0.42), new Double(0.425))
     *      .comparators(DOUBLE_SCALE.of(0.01))
     * </pre>
     * The result of comparison will be true because error is less than given 0.01.
     * Instance of given comparator wouldn't be cached and will be used only for this reflection call
     */
    public ReflectsMatcher comparators(Comparator... comparators) {
        this.comparators = comparators as List
        this
    }

    /**
     * Add concrete comparator to the list.
     * <p>
     * Some modes can be instantiated with especial params f.e. {@link com.ainrif.gears.spock_device.comparator.DOUBLE_SCALE}.
     * By default it's used with scale 1e-14 but it can be changed via instance of comparator:
     * <pre>
     * Tricorder.reflects(new Double(0.42), new Double(0.425))
     *      .comparator(DOUBLE_SCALE.of(0.01))
     * </pre>
     * The result of comparison will be true because error is less than given 0.01.
     * Instance of given comparator wouldn't be cached and will be used only for this reflection call
     */
    public ReflectsMatcher comparator(Comparator comparator) {
        this.comparators += comparator
        this
    }

    /**
     * Set list of fields to ignore diffs. Replaces all added before.
     * <p>
     * Diffs of given fields will be skipped. Nested names for POJO are also possible
     * <p>
     * Example:
     * <pre>
     * Tricorder.reflects(obj1, obj2)
     *   .excludeFields('fieldOne', 'fieldTwo.fieldInNestedObject')
     * </pre>
     */
    public ReflectsMatcher excludeFields(String... fields) {
        this.excludedFields = fields as List
        this
    }

    /**
     * Add filed name to exclude list.
     * <p>
     * Diffs of given fields will be skipped. Nested names for POJO are also possible
     * <p>
     * Example:
     * <pre>
     * Tricorder.reflects(obj1, obj2)
     *   .excludeField('fieldName.fieldInNestedObject')
     * </pre>
     */
    public ReflectsMatcher excludeField(String field) {
        this.excludedFields += field
        this
    }

    boolean asBoolean() {
        Difference difference = ExtendedReflectionComparatorFactory
                .create(comparators, modes)
                .getDifference(expected, actual)

        if (difference) {
            def report = new DefaultDifferenceView().createView(difference)
            (excludedReport, diffReport) = report.split(/\r?\n(?!\s)/)
                    .split { diff -> excludedFields.any { diff =~ /^$it(\[|\.|:).*/ } }
        }

        return !diffReport
    }

    @Override
    String toString() {
        return diffReport.join('\r\n')
    }
}
