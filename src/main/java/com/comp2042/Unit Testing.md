# **Unit Testing** 

Software Testing Life Cycle Code Development Unit Tests Build Service Integration Tests Deploy to QA Environment End to End Tests UI Tests Deploy to Production Environment Customer Happiness! 





Type of Testing REGRESSION TESTING is a type of testing that is done to verify that a code change in the software does not impact the existing functionality of the product. This is to make sure the product works fine with new functionality, bug fixes or any change in the existing feature. Previously executed test cases are re-executed in order to verify the impact of the change. Ref: https://www.softwaretestinghelp.com/regression-testing-tools-and-methods/ 









INTEGRATION TESTING is a level of software testing where individual units / components are combined and tested as a group. The purpose of this level of testing is to expose faults in the interaction between integrated units. Ref: https://softwaretestingfundamentals.com/integration-testing 





Type of Testing Alpha Testing is a type of software testing performed to identify bugs before releasing the product to real users or to the public. Alpha Testing is one of the user acceptance testing. Beta Testing is performed by real users of the software application in a real environment. Beta testing is one of the type of User Acceptance Testing. Ref: https://www.geeksforgeeks.org/difference-between-alpha-and-beta-testing 











UNIT TESTING 



Unit testing: the concept Unit testing focuses on testing the building blocks of the software. In OO programming, we can test the objects and methods. Why use it? 









Development: It allows us to focus on small units It makes it easier to locate errors We can test components separately (and in parallel) 









Maintenance: It builds us a test set we can use for regression testing Ie. checking for errors after we maintain or fix software 







The Cost of Defects Why Testing is Important Fix Earlier, reduce cost Graph showing cost increasing from Implementation -> Unit Testing -> Integration Testing -> System Testing -> In-service  Table: Design and architecture: 1X\* Implementation: 5X Integration testing: 10X Customer beta test: 15X Postproduct release: 30X \*X is a normalized unit of cost and can be expressed in terms of person-hours, dollars, etc. Source: National Institute of Standards and Technology (NIST)† By catching defects as early as possible in the development cycle, you can significantly reduce your development costs. Read More: https://xbsoftware.com/blog/cost-bugs-software-testing/ 











Is Unit Testing sufficient? No!  (Slide shows a diagram illustrating: Unit Testing, Integration Testing, Acceptance Testing) 







Think about: What to test...  We can't test everything! How do we choose what to test? 





Equivalence classes  Find a representative set of values. All other values from the same set should give an equivalent result. Guidelines 







If the input values is a range, choose 1 in the range and 2 outside 



If the input value is a set (e.g. LAPTOP, DESKTOP, PHONE) then may need separate tests if they behave differently, plus one outside the set (e.g. CHARGER) 



If a specific condition is required e.g. "Must have a capital letter", choose one which is positive and one which is negative.  The Art of Software Testing, Second Edition. G. Myers, 2004. 





Think about: What to test...  We can't test everything! How do we choose what to test?  2. Boundary conditions Values around boundaries seem to be important. For example, in a range of numbers, we need to be careful about values just before and just after the range.  E.g. if the accepted range is -2.0 to +2.0, test e.g. -2.0,-2.01, 2.0, 2.01 











Think about: What to test...  We can't test everything! How do we choose what to test?  3. Special values For numbers, 0 is often a special case Time  4. Logic (decision coverage) Develop tests to exercise all logical paths - e.g both parts of an if-else statement 











Examples, which test cases to choose? 



A password string which shouldn't contain a number 



Input is one item specifying the day of the week (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY) 



An age in years, in range 50 to 75 



Input is APPLE, BANANA, GRAPES, PEAR and a number of items (e.g. GRAPES, 50) 



A profit value, from -$100.00 to +$100.00 



A time in the form HH:MM 



Test Driven Development But what's wrong with writing the Unit tests after the code? Could be "written to pass"? Delays testing until the end of a coding session Refactoring difficult until you have tests Alternative: Test Driven Development: write the test FIRST!! What does writing the test first allow us to do? Test can be derived from business requirements, so it makes sure we meet the spec Write the minimum amount of code to pass RED GREEN REFACTOR 











Test Driven Development Example: Customer "Make me a table for my office" You (1): (Non-TDD) Go away and build any old table and see if it meets the customers needs. You (2): (TDD) Think: How do I build tests to test my table: 











Does it have > 2 legs? 



Is it flat? 



Is it at least 2m long and 1m wide? 



Does it support 50 kg? Build something to pass all these tests, and you have a table! If you change the spec later, you can make sure your new design still works as a table using the tests



















# **Basic Maintenance and Documenting Code with Javadoc**

COMP2042 Developing Maintainable Software Lecture 07 Basic Maintenance, Documentation



Content Topics covered here: Basic Maintenance Documenting Code with Javadoc



(UML Diagram Classes and Interfaces) Database Technician Zookeeper Doctor ZooApp ZooCorp <<Interface>> Employable Employee Admin Manager Visitor <<Interface>> Visitable Zoo Group Compound Animal Consultant <<Interface>> Maintainable Penguin Flamingo Parrot Bird Mammal Reptile Amphibian Fish Invertebra <<Interface>> Feedable Food Meat Vitamines Plant



Basic Maintenance What would you propose how we should get started?



Basic Maintenance Use Eclipse (Refactor Menu) Rename... Alt+Shift+R Move... Alt+Shift+V Change Method Signature... Alt+Shift+C Extract Method... Alt+Shift+M Extract Local Variable... Alt+Shift+L Extract Constant... Inline... Alt+Shift+I Convert Local Variable to Field... Convert Anonymous Class to Nested... Move Type to New File... Extract Interface... Extract Superclass... Use Supertype Where Possible... Pull Up... Push Down... Extract Class... Introduce Parameter Object... Introduce Indirection... Introduce Factory... Introduce Parameter. Encapsulate Field... Generalize Declared Type... Infer Generic Type Arguments... Migrate JAR File... Create Script... Apply Script... History...



(Source Menu) Toggle Comment Ctrl+/ Add Block Comment Ctrl+Shift+/ Remove Block Comment Ctrl+Shift+



Generate Element Comment Alt+Shift+J Shift Right Shift Left Correct Indentation Ctrl+I Format Ctrl+Shift+F Format Element Add Import Ctrl+Shift+M Organize Imports Ctrl+Shift+O Sort Members... Clean Up... Override/Implement Methods... Generate Getters and Setters... Generate Delegate Methods... Generate hashCode() and equals()... Generate toString()... Generate Constructor using Fields... Generate Constructors from Superdass... Surround With Alt+Shift+Z Externalize Strings... Find Broken Externalized Strings



Basic Maintenance Use IntelliJ (Code Menu) Override Methods... Ctrl+0 Implement Methods... Ctrl+I Delegate Methods... Generate... Alt+Insert Surround With... Ctrl+Alt+T Unwrap/Remove... Ctrl+Shift+Delete Completion Folding Insert Live Template... Ctrl+J Surround with Live Template... Ctrl+Alt+J Comment with Line Comment Ctrl+/ Comment with Block Comment Ctrl+Shift+/ Reformat Code Ctrl+Alt+L Show Reformat File Dialog Ctrl+Alt+Shift+L Auto-Indent Lines Ctrl+Alt+I Optimize Imports Ctrl+Alt+O Rearrange Code Move Statement Down Ctrl+Shift+Down Move Statement Up Ctrl+Shift+Up Move Element Left Ctrl+Alt+Shift+Left Move Element Right Ctrl+Alt+Shift+Right Move Line Down Alt+Shift+Down Move Line Up Alt+Shift+Up Update Copyright... Generate module-info Descriptors Convert Java File to Kotlin File Ctrl+Alt+Shift+K



(Refactor Menu) Refactor This... Ctrl+Alt+Shift+T Rename... Shift+F6 Rename File... Change Signature... Ctrl+F6 Type Migration... Ctrl+Shift+F6 Make Static... Convert To Instance Method... Move... F6 Copy... F5 Safe Delete... Alt+Delete Extract Inline... Ctrl+Alt+N Find and Replace Code Duplicates... Invert Boolean... Pull Members Up... Push Members Down... Use Interface Where Possible... Replace Inheritance with Delegation... Remove Middleman... Wrap Method Retum Value... Convert Anonymous to Inner. Encapsulate Fields... Replace Temp with Query... Replace Constructor with Factory Method... Replace Constructor with Builder... Generify... Migrate... Internationalize... Modularize



Basic Maintenance Dividing all classes into packages Why is this useful? Providing individual name spaces Making large project easier to handle (better organised) Dividing responsibilities amongst colleagues Commenting code and producing Javadocs



Basic Maintenance Packages are used in Java in order to prevent naming conflicts, to control access, to make searching, locating and usage of classes, interfaces, enumerations and annotations easier, etc.



(Image shows a flat package structure: com.siebers.ZooProject.Main containing Admin.java, Amphibian.java, Animal.java, etc.)



(Image shows an organized package structure) com.zooproject com.zooproject.animals com.zooproject.db com.zooproject.employees com.zooproject.food com.zooproject.misc com.zooproject.visitors com.zooproject.zoos



Basic Maintenance Communication between packages



(Code Example 1: No import) package com.siebers; public class Main { public static void main(String args) { Employee e1 new Employee(); } }



(Code Example 2: With import) package com.siebers; import com.siebers.dep1.Employee; public class Main { public static void main(String args) { Employee e1=new Employee(); com.siebers.dep2.Manager m1=new com.siebers.dep2.Manager(); } }



(Package structure shown) com.siebers



Main.java com.siebers.dep1 Employee.java Manager.java com.siebers.dep2 Employee.java Manager.java



JUnit Unit testing



What is JUnit? JUnit is a popular open-source testing framework for Java, widely used for unit testing Java applications. It allows developers to write and execute tests easily, ensuring that individual parts of the software (like methods or classes) work as expected. JUnit encourages Test-Driven Development (TDD), where developers write tests before writing the actual code. Once the tests are written, they can be executed automatically. This is useful when working with a large codebase, as you can run all tests to ensure that new changes don't break existing functionality (this is called regression testing).



JUnit Annotations JUnit uses annotations to define and manage test cases. These annotations help organize the tests, run setup/teardown code, and define test behaviors like exception handling or ignoring specific tests. Common Annotations: @Test: Marks a method as a test method. JUnit will run methods annotated with this. @Before, @After: These annotations are used for setup and teardown operations that run before and after each test, respectively. @BeforeClass, @AfterClass: Used to execute setup and cleanup methods once for all tests, such as initializing resources that are shared across tests. @Ignore: Skips a test method if the method is not ready or needs to be ignored temporarily.



JUnit Assertions JUnit provides a set of assertion methods to check whether the code behaves as expected. Assertions verify the actual result against the expected result. If an assertion fails, the test case fails. assertEquals(expected, actual): Verifies that two values are equal. assertTrue(condition): Asserts that a condition is true. assertFalse(condition): Asserts that a condition is false. assertNull(object): Asserts that an object is null. assertNotNull(object): Asserts that an object is not null. assertThrows(): Verifies that a method throws a specific exception.



Documenting Code Javadoc and beyond



Document your source code In software programming it is important to document the source code, because: you will have a clear picture of complex Java projects with many classes or modules; you will be able, later, to understand what was done so that you can modify, add or delete it.



Going beyond manual code comments Auto documentation allows for : Consistent formatting and structuring of comments Widespread readability You don't have to do it all yourself Examples include Doxygen and Javadocs Doxygen can be used for c++, with modified versions used for Scala and C# Javadocs are for java, Hadoc is for Haskell.



Eclipse/IntelliJ are really helpful IDEs for comments It is useful beyond providing us with a nice interface, project viewer and telling us of precompile errors, missing libraries and spelling mistakes. Javadoc is a great tool for java documentation.



This comes with the JDK and requires you to tag your code with special comments. Has special syntax to differentiate it from regular comments /\*\*......../ as opposed to /...\*/



Useful Javadoc Tags Syntax: @<tag> It generates a really easy to use HTML based output as a living document. Updated each time you compile if Javadoc is on the compilation path. Some famous tags: @param: to explain a parameter @return: to annotate a return value @throws/@exception: for your error handling @deprecated: bits of the code you no longer use {@code}: puts syntax in your documentation



Javadoc comment rules The basic rule for creating JavaDoc comments is that they begin with /\*\* and end with \*/. You can place JavaDoc comments in any of three different locations in a source file: Immediately before the declaration of a public class. Immediately before the declaration of a public field. Immediately before the declaration of a public method or constructor.



Basic Maintenance (Code example with Javadoc) package com.zooproject.zoos; import java.util.ArrayList; import com.zooproject.animals.Animal;



/\*\*



Objects of this class represent zoo compounds



@author Peer-Olaf Siebers



@version 2.3



@since 1.0 \*/ public class Compound { private ArrayList<Animal> animals;



public Compound() { animals new ArrayList<>(); }



/\*\*



This method adds animals to the animal ArrayList.



@param animal An animal that lives in any of the zoos. \*/ public void addAnimal (Animal animal) { animals.add(animal); } }



Add the right comments, and Eclipse/ IntelliJ has a wizard to generate javadoc documentation for you (Menu options shown: Generate Javadoc...)



Basic Maintenance (Images of IntelliJ and Eclipse "Generate Javadoc" wizards) Notice the configure button and destination. You can create javadoc for members such as Public, Protected, Package and even Private.



Basic Maintenance (Image of generated Javadoc HTML output) Class Compound java.lang.Object com.zooproject.zoos.Compound



public class Compound extends java.lang.Object



Objects of this class represent zoo compounds Since: 1.0 Version: 2.3 Author: Peer-Olaf Siebers



Constructor Summary Constructor | Description Compound()



Method Summary Modifier and Type | Method | Description void | addAnimal(com.zooproject.animals.Animal animal) | This method adds animals to the animal ArrayList.



