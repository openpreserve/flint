<?xml version="1.0"?>
<!--
Schematron rules for policy-based  validation of PDF, based on output of Apache Preflight.

The current set of rules represents the following policy:
   * No encryption / password protection
   * All fonts are embedded and complete
   * No JavaScript
   * No embedded files (i.e. file attachments)
   * No multimedia content (audio, video, 3-D objects)
   * No PDFs that raise exception or result in processing error in Preflight (PDF validity proxy)

All Preflight error codes are documented here:
   http://svn.apache.org/repos/asf/pdfbox/trunk/preflight/src/main/java/org/apache/pdfbox/preflight/PreflightConstants.java

See also:
   http://wiki.opf-labs.org/display/TR/Portable+Document+Format

Changes relative to pdf_policy_preflight_test.sch: tried to make error descriptions more concise

-->
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron">
  <s:pattern name="Check for existence of Preflight element">
    <s:rule context="/">
      <s:assert test="preflight">Preflight root element missing</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Check for Preflight exceptions">
    <s:rule context="/preflight">
      <s:assert test="not(exceptionThrown)">Preflight exception</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Check for unknown errors">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '-1')">Unknown error</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Check for malformed PDF and general processing errors">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '8')">Processing error (possibly malformed PDF)</s:assert>
      <s:assert test="not(code = '8.1')">Mandatory element missing (possibly malformed PDF)</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Checks for encryption">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '1.0' and contains(details,'password'))">Open password</s:assert>
      <s:assert test="not(code = '1.4.2')">Encryption</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Check for font error, unspecified">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '3')">Unspecified font error</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Checks for invalid or incomplete font dictionaries">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '3.1')">Invalid data in font dictionary</s:assert>
      <s:assert test="not(code = '3.1.1')">Mandatory fields missing from font dictionary</s:assert>
      <s:assert test="not(code = '3.1.2')">Mandatory fields missing from font descriptor dictionary</s:assert>
      <s:assert test="not(code = '3.1.3')">Error in font descriptor</s:assert>
      <!-- Errors 4, 5 and 6 are common and apparently not serious, so you may want to comment them out -->
      <s:assert test="not(code = '3.1.4')">Charset declaration missing in Type 1 subset</s:assert>
      <s:assert test="not(code = '3.1.5')">Encoding inconsistent with font</s:assert>
      <s:assert test="not(code = '3.1.6')">Width array and font program width inconsistent</s:assert>
      <!-- -->
      <s:assert test="not(code = '3.1.7')">Required entry missing in composite font dictionary</s:assert>
      <s:assert test="not(code = '3.1.8')">Invalid CIDSystemInfo dictionary</s:assert>
      <s:assert test="not(code = '3.1.9')">Invalid CIDToGID</s:assert>
      <s:assert test="not(code = '3.1.10')">Missing or invalid CMap in composite font</s:assert>
      <s:assert test="not(code = '3.1.11')">Missing CIDSet entry in subset of composite font</s:assert>
      <s:assert test="not(code = '3.1.12')">Missing or invalid CMap in composite font</s:assert>
      <s:assert test="not(code = '3.1.13')">Encoding entry can't be read due to IOException</s:assert>
      <s:assert test="not(code = '3.1.14')">Unknown font type</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Checks for damaged embedded fonts">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '3.2')">Damaged embedded font</s:assert>
      <s:assert test="not(code = '3.2.1')">Damaged embedded Type1 font</s:assert>
      <s:assert test="not(code = '3.2.2')">Damaged embedded TrueType font</s:assert>
      <s:assert test="not(code = '3.2.3')">Damaged embedded composite font</s:assert>
      <s:assert test="not(code = '3.2.4')">Damaged embedded type 3 font</s:assert>
      <s:assert test="not(code = '3.2.5')">Damaged embedded CID Map</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Checks for glyph errors">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '3.3')">Glyph problem</s:assert>
      <s:assert test="not(code = '3.3.1')">Missing glyph</s:assert>
      <s:assert test="not(code = '3.3.2')">Missing glyph</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Check for JavaScript">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '6.2.5' and contains(details,'JavaScript'))">JavaScript</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Checks for embedded files and file attachments">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '1.4.7')">Embedded file(s)</s:assert>
      <s:assert test="not(code = '1.2.9')">Embedded file(s)</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern name="Checks for multimedia content">
    <s:rule context="/preflight/errors/error">
      <s:assert test="not(code = '5.2.1' and contains(details, 'Screen'))">Screen annotation</s:assert>
      <s:assert test="not(code = '5.2.1' and contains(details, 'Movie'))">Movie annotation</s:assert>
      <s:assert test="not(code = '5.2.1' and contains(details, 'Sound'))">Sound annotation</s:assert>
      <s:assert test="not(code = '5.2.1' and contains(details, '3D'))">3D annotation</s:assert>
      <s:assert test="not(code = '6.2.5' and contains(details, 'Movie'))">Movie action</s:assert>
      <s:assert test="not(code = '6.2.5' and contains(details, 'Sound'))">Sound action</s:assert>
      <s:assert test="not(code = '6.2.6' and contains(details, 'undefined'))">Undefined action (e.g. Rendition)</s:assert>
    </s:rule>
  </s:pattern>

  <!-- Optional: report any other Preflight errors as warnings, disabled for now (also this partially overlaps with above)
  <s:pattern name="Miscellaneous warnings">
    <s:rule context="/preflight/errors/error">
      <s:report test="starts-with(code,'1')">Syntax error(s)</s:report>
      <s:report test="starts-with(code,'2')">Graphics error(s)</s:report>
      <s:report test="starts-with(code,'3')">Font error(s)</s:report>
      <s:report test="starts-with(code,'4')">Transparency error(s)</s:report>
      <s:report test="starts-with(code,'5')">Annotation error(s)</s:report>
      <s:report test="starts-with(code,'6')">Action error(s)</s:report>
      <s:report test="starts-with(code,'7')">Metadata error(s)</s:report>
    </s:rule>
  </s:pattern>
  -->
</s:schema>
