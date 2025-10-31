---
title: "Keys and indexes"
math: false
weight: 31
---

Sometimes we need to map more aspects of the logical model - mainly keys of the properties and indexes in arrays. In order to do that, we define *canonical* and *short* forms of access paths.

## Canonical form

This forms represents all keys and indexes explicitly. It's used by algorithms like ModelToCategory because it's much easier to work with.

### Key

A key is the name of the property in the logical model. E.g., a customer can have name and any number of contact properties (i.e., a map):
```js
{
    "name": "Joe",              // normal property
    "phone": "123456789",       // key: "phone"
    "email": "joe@example.com", // key: "email"
    ...
}
```
It's probably not a good practice to have the map share the same level as other properties, but it's possible. In its canonical form, the property might be represented like this:
```js
{
    name: 1,
    $dynamic: -2 {
        $key: 3,
        $value: 4,
    },
}
```

Notice the three special properties:
- `$dynamic` - indicates that multiple instances can match this property. The signature of the `$dynamic` property has to be an array, because there is in fact an array of key-value pairs.
- `$key` - indicates the signature of the key property.
- `$value` - indicates the signature of the value property.

The dynamic name can also have a *pattern* which limits which keys it can match. This is useful when combining multiple maps in one structure. We would write it like `$dynamic(pattern)`.

Lastly, even though the property is *complex* (i.e., it's an object with `$key` and `$value`), it still represents a *simple* property in the logical model. This is disadvantage of the canonical form - the structure of the "real data" isn't immediately obvious.

### Index

An index is used to access elements in an array. Consider this example:
```js
{
    "name": "Joe",
    "hobbies": [
        "programming",  // index: 0
        "hiking",       // index: 1
        ...
    ]
}
```
In its canonical form, we can represent it as:
```js
{
    name: 1,
    hobbies: -5 {
        $index(0): 6,
        $value: 7,
    },
}
```
This is very similar to the dynamic name representation. The key difference is that now there is a normal string name `hobbies` so the information about the index capture can't be store in the name itself. The `$index(0): 6` means that signature `6` corresponds to the first index of the array. More on multiple indexes later.

## Short form

The short form aims to accurately represent the "look" of the real data. Let's look at the previous examples (combined) in their short form:
```js
{
    name: 1,
    <>: -2 <3> 4,
    hobbies: -5 [6] 7,
}
```
The dynamic name with pattern would be written like `<pattern>`.

The signatures are separated to indicate which part corresponds to which property. Also, we know that whenever there is a normal signature followed by `<>` or `[]`, it should be replaced by a complex property (in the canonical form).

### Combining keys and indexes

The short form allows us to write something like this:
```js
{
    name: 1,
    <>: -2 <3> -4 [5][6] -7 [8] 9,
}
```
This unholy abomination is transformed to the canonical form like this:
```js
{
    name: 1,
    $dynamic: -2 {
        $key: 3,
        $value: -4 {
            $index(0): 5,
            $index(1): 6,
            $value: -7 {
                $index(0): 8,
                $value: 9,
            },
        },
    },
}
```
So we can easily combine maps, arrays, and multi-dimensional arrays.

### Combining with normal properties

As a shortcut, we might want to combine normal properties with keys and indexes. For example, we might be tempted to write something like this:
```js
{
    name: 1,
    friends: -2 {
        $index(0): 3,
        $value: 4,      // name of the friend
        since: 5,       // when we became friends
    }
}
```
However, this is not possible. We can't simply replace it by its short form, which would mean the real data can't look like this. We have to use this canonical form instead:
```js
{
    name: 1,
    friends: -2 {
        $index(0): 3,
        $value: ϵ {
            name: 4,
            since: 5,
        },
    }
}
```
A short form representation should therefore be:
```js
{
    name: 1,
    friends: -2 [3] ϵ {
        name: 4,
        since: 5,
    },
}
```

Another limitation is that keys can't be combined with indexes. E.g., this is not possible:
```js
{
    name: 1,
    $dynamic: -2 {
        $key: 3,
        $index(0): 4,
        $value: 5,
    }
}
```
We have to use something like this instead:
```js
{
    name: 1,
    $dynamic: -2 {
        $key: 3,
        $value: -4 {
            $index(0): 5,
            $value: 6,
        },
    }
}
```
Notice the new structure requires a different schema category - there is now an additional property for the array (and a morphism connecting it to the map).
