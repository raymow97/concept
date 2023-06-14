package com.github.linyuzai.domain.core;

import com.github.linyuzai.domain.core.condition.Conditions;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 领域工厂
 */
public interface DomainFactory {

    <T extends DomainObject> T createObject(Class<T> cls, String id);

    <T extends DomainObject> T createObject(Class<T> cls, Conditions conditions);

    <T extends DomainObject> T createObject(Class<T> cls, Supplier<T> supplier);

    <T extends DomainObject> T createObject(Class<T> cls, DomainCollection<T> collection, String id);

    <T extends DomainObject> T createObject(Class<T> cls, DomainCollection<T> collection, Predicate<T> predicate);

    <T extends DomainObject, C extends DomainCollection<T>> Map<String, T> createObject(Class<C> cls, Collection<String> ownerIds, Function<Collection<String>, Map<String, String>> idMapping);

    <T extends DomainObject, C extends DomainCollection<T>> Map<String, T> createObject(Class<T> dCls, Class<C> cCls, Collection<String> ownerIds, Function<Collection<String>, Map<String, String>> idMapping);

    <C extends DomainCollection<?>> C createCollection(Class<C> cls, Collection<String> ids);

    <C extends DomainCollection<?>> C createCollection(Class<C> cls, Conditions conditions);

    <T extends DomainObject, C extends DomainCollection<T>> C createCollection(Class<C> cls, Supplier<Collection<T>> supplier);

    <T extends DomainObject, C extends DomainCollection<T>> C createCollection(Class<C> cls, C collection, Collection<String> ids);

    <T extends DomainObject, C extends DomainCollection<T>> C createCollection(Class<C> cls, C collection, Predicate<T> predicate);

    <T extends DomainObject, C extends DomainCollection<T>> Map<String, C> createCollection(Class<C> cls, Collection<String> ownerIds, Function<Collection<String>, Map<String, ? extends Collection<String>>> idsMapping);

    <T extends DomainObject, C extends DomainCollection<T>> Map<String, C> createCollection(Class<T> dCls, Class<C> cCls, Collection<String> ownerIds, Function<Collection<String>, Map<String, ? extends Collection<String>>> idsMapping);

    <T extends DomainObject> T wrapObject(Class<T> cls, T object);

    <C extends DomainCollection<?>> C wrapCollection(Class<C> cls, Collection<? extends DomainObject> objects);

    <C extends DomainCollection<?>> C emptyCollection(Class<C> cls);
}
