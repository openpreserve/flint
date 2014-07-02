Feature: Run a simple job using an implementation of the core Format API

  Scenario: Run a simple job without making use of policy validation, with a format validator that is always happy.

    Given I have a class that implements Format and knows about the "SIMPLE_FORMAT" format
    And which has only one check category "ALL_GOOD" that is always happy, if not supplied with a schematron policy

    And I have a file "aFile.SIMPLE_FORMAT" with the following content:
      """
      <head>
        I am the head.
      </head>
      <belly>
        I am the belly.
      </belly>
      <toes>
        We are the toes.
      </toes>
      """

    When I call the command line CoreApp specifying the file's location "aFile.SIMPLE_FORMAT"

    Then a results file "results.xml" should be produced with the following content:
      """
      <?xml version='1.0' encoding='utf-8'?>
      <flint>
          <checkedFile name='aFile.SIMPLE_FORMAT' result='passed' format='SIMPLE_FORMAT' version='0.1.2.3.4.5' totalCheckTime='1'>
              <checkCategory name='testCat' result='passed'>
                  <check name='testCheck' result='passed'/>
              </checkCategory>
          </checkedFile>
      </flint>
      """

  Scenario: Run a simple job, similar to as above but providing a simple schematron policy.

    Given I have a class that implements Format and knows about the "SIMPLE_FORMAT_WITH_POLICY" format
    And which exclusively does policy-based validation and uses the following schema:
      """
      <?xml version='1.0'?>
      <s:schema xmlns:s='http://purl.oclc.org/dsdl/schematron'>
        <s:pattern name='Body count'>
          <s:rule context='head'>
            <s:assert test='count(beard) = 1'>You shall be no beardless being</s:assert>
            <s:assert test='count(piercing) = 1'>You shall not be missing a piercing</s:assert>
          </s:rule>
          <s:rule context='body'>
            <s:assert test='count(belly) = 1'>A belly has to be</s:assert>
          </s:rule>
        </s:pattern>
      </s:schema>
      """

    And I have a file "aFile.SIMPLE_FORMAT_WITH_POLICY" with the following content:
      """
      <body>
        <head>
          I am the head.
          <beard>
            Yes, I have a beard
          </beard>
        </head>
        <belly>
          I am the belly.
        </belly>
        <toes>
          We are the toes.
        </toes>
      </body>
      """

    When I call the command line CoreApp specifying the file's location "aFile.SIMPLE_FORMAT_WITH_POLICY"

    Then a results file "results.xml" should be produced with the following content:
      """
      <?xml version='1.0' encoding='utf-8'?>
      <flint>
          <checkedFile name='aFile.SIMPLE_FORMAT_WITH_POLICY' result='failed' format='SIMPLE_FORMAT_WITH_POLICY' version='0.1.2.3.4.5' totalCheckTime='0'>
              <checkCategory name='Body count' result='failed'>
                  <check name='You shall be no beardless being' result='passed' errorCount='0'/>
                  <check name='You shall not be missing a piercing' result='failed' errorCount='1'/>
                  <check name='A belly has to be' result='passed' errorCount='0'/>
              </checkCategory>
          </checkedFile>
      </flint>
      """