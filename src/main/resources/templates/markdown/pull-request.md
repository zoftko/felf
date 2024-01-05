## Binary Analysis

### Size
| Type | This PR   | Default Branch      | Difference          |
|------|-----------|---------------------|---------------------|
| Text | ${prText} | ${(mainText)!"N/A"} | ${(diffText)!"N/A"} |
| Data | ${prData} | ${(mainData)!"N/A"} | ${(diffData!"N/A")} |
| BSS  | ${prBss}  | ${(mainBss)!"N/A"}  | ${(diffBss)!"N/A"}  |

<#if firstTime>
Note: It seems this is the first time running ELF Watch on this project, once merged you will be able to obtain
comparisons between your default branch and pull requests.
</#if>

*Analyzed SHA: ${sha}*.
