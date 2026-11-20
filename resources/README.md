# Validation resources for the panel demonstration

These are actual input files consumed by CardFileLoader, not example output. Open a file in IntelliJ, point out its contents, then run the matching command from the project root.

| Resource | What to show | Expected result |
| --- | --- | --- |
| input.txt | Four lines of 13 cards, all distinct | Accepted: 52 unique cards |
| duplicate.txt | H-A at line 1 and again at the end of line 4 | Duplicate card H-A on line 4 |
| invalid.txt | Z-K on line 1 | Unknown suit Z |
| invalid-rank.txt | D-15 on line 1 | Unknown rank 15 |
| invalid-format.txt | D2 without a hyphen | Invalid format D2 |
| incomplete.txt | Only four cards | Expected 52 cards, found 4 |

## Show one case

```powershell
./run.ps1 validate duplicate.txt
```

The bare filename is resolved in this folder. An explicit path works too:

```powershell
./run.ps1 validate resources/input.txt
```

## Run all cases

```powershell
./demo-validation.ps1
```

The seventh case deliberately names a missing file. It has no resource file because its purpose is to demonstrate the missing-file error.

## Explain the logic

Read file -> split tokens -> validate suit-rank format -> match enum values -> reject duplicate identities with HashSet -> require 52 unique cards -> create Deck.

Validation stops at the first error. File failures and invalid card data are handled separately. Interactive play lets the user retry; the validate command reports an exit code for the demo script to check. These external demo resources are read from disk, not bundled classpath resources.
