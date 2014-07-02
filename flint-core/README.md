
# flint-core
## The Format interface
The central element in FLint is the Format interface. Implementing this interface is also the minimal requirement to build a new module that can make use of the FLint ecosystem. It makes sure an implementation knows which file-types it can assess, that it is known to the other FLint modules and that it can bring back validation results. How this is done is completely open to the implementation.

## PolicyAware?
Extending the abstract class PolicyAware connects a Format implementation to the world of policy-focused schematron-based validation. The only method that needs to be implemented (getPolicy) has to provide the schematron file containing the validation instructions (see flint-pdf or flint-epub for examples).

## CheckResults: a standardised output
FLint CheckResults come with a three-level validation approach:

| CheckResult                            | CheckCategories               | CheckCheck                 |
| -------------------------------------- | ----------------------------- | -------------------------- |
| one per format implementation and file | one to many per CheckResult   | one to many per CCategory  |
|                                        |                               |                            |
| info about overall result, filename,.. | in schematron terms a pattern | in schematron terms a test |
|                                        |                               |                            |
| result only 'passed' if all categories | result only 'passed' if all   | result reflects code or    |
| pass as well                           | checks pass as well           | policy logic               |

## TimedValidation and -Tasks: what if the validation process fails on a currupt file?
Using third party software for validation on top of potentially very corrupt files can't exclude the possibility of it crashing very badly.
A Format implementation has the option to perform the communication with the different bits of validation logic via a TimedValidation, the actual validation code wrapped in subclasses of the abstract class TimedTask. This guarantees that any occurring unexpected exception is being caught and doesn't cause the whole thing to crash. Also, a timeout can be set to avoid infinitive loops.
This functionality is specifically important in cases where FLint is used on scale as via flint-hadoop.

## FLint
The FLint class brings it all together. It knows about the available Format implementations, calls them to check the provided files and can print out the CheckResults.
