.. java:import:: java.util.concurrent ConcurrentHashMap

.. java:import:: java.util.function Function

.. java:import:: java.util.function Predicate

.. java:import:: lombok.experimental UtilityClass

CollectionUtils
===============

.. java:package:: io.github.ust.mico.core.util
   :noindex:

.. java:type:: @UtilityClass public class CollectionUtils

   Provides some utility functions for easy creation of collections.

Methods
-------
distinctByKey
^^^^^^^^^^^^^

.. java:method:: public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor)
   :outertype: CollectionUtils

   Inspired by Stuart Marks https://stackoverflow.com/a/27872852/9556565

listOf
^^^^^^

.. java:method:: @SafeVarargs public final <T> List<T> listOf(T... items)
   :outertype: CollectionUtils

mapOf
^^^^^

.. java:method:: public final <K, V> Map<K, V> mapOf(K key, V value)
   :outertype: CollectionUtils

mapOf
^^^^^

.. java:method:: public final <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2)
   :outertype: CollectionUtils

mapOf
^^^^^

.. java:method:: public final <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3)
   :outertype: CollectionUtils

mapOf
^^^^^

.. java:method:: public final <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4)
   :outertype: CollectionUtils

