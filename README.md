# Lango

My first try into understanding how interpreters and compilers work with the help of [Crafting Interpreters](1) by Bob Nystrom.

## Features

- [x] Variables.
- [x] Control Flow constructs like conditional statements and loops.
- [x] Functions with parameters.
- [x] Anonymous Functions.
- [x] Closures.
- [x] Classes.
- [x] Constructors.
- [x] Fields.
- [x] Methods.
- [x] Inheritance.

## How to run a Lango script

Type `bash ./bin/compileAndRunScript` so you can execute `script.lango` file.

## Snippets

### Variables

```print 9 * 5 + 5;

 var a = 10;

 print a;
```

### Scopes and Environments

```
  var a = "global a";
  var b = "global b";
  var c = "global c";
  {
    var a = "outer a";
    var b = "outer b";
    {
      var a = "inner a";
      print a;
      print b;
      print c;
    }
    print a;
    print b;
    print c;
  }
  print a;
  print b;
  print c;

```

---

### If/Else If Statement

```
var i = 5;
if(i >= 5) {
  print i + " less than 5";
} elif( i > 5) {
  print i + " equal to 5";
} elif(i <= 5) {
  print i + " equal to  x22";
} else {
  print i + " bigger or equal than 5";
}

```

---

### For/While Loops

```
var i = 1;
while(i <= 5) {
 print i;
 if(i == 4) break;
 i = i + 1;
}
var a = 0;
var temp;
for (var b = 1; a < 10000; b = temp + b) {
  print a;
  temp = a;
  a = b;
}
for (var i = 1; i <= 5; i = i + 1) {
  print i;
}
```

---

### Functions

```
 fun sum(a, b){
   return a + b;
 }
 var result = sum(1, 2);
 print result;
 fun printer(function, n1, n2) {
   print(function(n1, n2));
 }
 printer( fun(n1, n2) {
   return n1 + n2;
   }, 4, 5);
 printer( fun(n1, n2) {
   return n1 * n2;
   }, 4, 5);
```

---

### Closure

```fun makeCounter() {
var i = 0;
fun count() {
i = i + 1;
print i;
}
return count;
}
var counter = makeCounter();
counter(); // "1".
counter(); // "2".
```

---

### Resolver

```
var a = "global";
{
fun showA() {
print a;
}
showA();
var a = "block";
showA();
}
Syntactic errors.
return;
print this;

```

---

### Classes and constructors

```
class Animal {
  init(name, type, age) {
  this.name = name;
  this.type = type;
  this.age = age;
}
  printInfo() {
    print "My name is " + this.name + " and my type   is " + this.type + " and my age is " + this.age;
  }
  sound() {
    print "Generic animal sound";
  }
}

var cat = Animal("Tota", "Cat", 4);
cat.printInfo();

```

### Inheritance

```
 class A {
  method() {
    print "A method";
  }
 }

 class B < A {
  init(testProp, testProp2) {
    this.testProp = testProp;
    this.testProp2 = testProp2;
  }
  method() {
    print "B method";
  }
  test() {
    super.method();
  }
 }

 class C < B {
  init(testParam1, testParam2) {
  super.init(testParam1, testParam2);
 }

 printCStuff() {
  print this.testProp;
  print this.testProp2;
  }
 }

 var aObject = A();

 aObject.method();

 var c = C("test parameter 1", "test parameter 2");

 print c.testProp2;

 var method = c.test;

 method();

 c.printCStuff();
```
