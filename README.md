# Relational DBMS
## Attribute
##### Constructors
```java
public Attribute(String name)
```
- description: initialize an attribute, and add it to the pool of attributes
- name: name of the attribute
```java
public static Attribute addAttribute(String name)
```
- description: add to the pool of attributes

##### Methods
```java
public AttributeSet closureUnder(FunctionalDependencySet f)
```
- description: return the closure of a set of attributes under F
- f: any set of functional dependency
