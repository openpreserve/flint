Feature: Creating policy properties files from schematron policies provided by submodules implementing the core Format API

  Scenario: Create a properties file for the MY_DREAM format
    Given I have a class that implements Format and knows about the "MY_DREAM" format
    And the related schematron policy file is:
      """
      <?xml version="1.0"?>
      <s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron">
        <s:pattern name="searching for a dream">
          <s:rule context="/">
            <s:assert test="spot_the_dream">noooooo! no dream found!</s:assert>
          </s:rule>
        </s:pattern>

        <s:pattern name="find unconscious anxieties">
          <s:rule context="/deeeeepInside">
            <s:assert test="not(afraid)">Scary! Have found fear!</s:assert>
            <s:assert test="not(traumatised)">Uh! Childhood trauma.</s:assert>
          </s:rule>
        </s:pattern>
      </s:schema>
      """
    When I call the command line properties file creator with the argument "MY_DREAM"
    Then a file with the name "MY_DREAM-policy.properties" should be created with the following content:
      """
      # This properties file is used to filter for specific asserts in the policy validation.
      # All asserts that are set to 'true' will be evaluated.

      # Pattern: searching for a dream
      ## Rule (context): /
      ## Assert (test): spot_the_dream
      searching\ for\ a\ dream=true

      # Pattern: find unconscious anxieties
      ## Rule (context): /deeeeepInside
      ## Assert (test): not(afraid)
      ## Assert (test): not(traumatised)
      find\ unconscious\ anxieties=true
      """