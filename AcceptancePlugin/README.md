# AcceptancePlugin

AcceptancePlugin is an Android Studio Plugin to generate Feat test code


## How to use

Plugin has two actions:

- Generate Feat (Test) 
- Generate Test Class for feats

### Generate Feat (Test) action

- Visible on right click or Generate Test menu on any `*.feature` file
- It prompts a directory chooser to mark where the code will be generated
- Tries to infer package from directory chooser (if directory has 'java' or 'kotlin' folder parent)
- Prompts a package input dialog if package can not be inferred.

#### Generated code

```
import com.intelygenz.android.*

@Feat(feature = "example.feature", scenario = "I am at the home screen")
class IAmAtTheHomeScreen {
    
    @Given("I logged in the app")
    fun given_I_Logged_In_The_App() = Unit

    @And("I am at the \"home screen\"")
    fun andIAmAtTheHomeScreen() = Unit

}

```

### Generate Test Class for feats action

- Visible on right click or Generate Test menu on folder containing `*.feature` files and a `.template` file 
- It prompts a directory chooser to mark where the code will be generated
- Tries to infer package from directory chooser (if directory has 'java' or 'kotlin' folder parent)
- Prompts a package input dialog if package can not be inferred.
- Plugin uses `.template` to generate test class code.

#### `.template` code

```
import com.intelygenz.android.dsl.classpath.ScenarioRunner
import com.intelygenz.android.dsl.classpath.runScenario
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(ScenarioRunner::class)
class AllScenariosTest {

{BODY}

}
```

{BODY} variable is used to insert test functions


#### Generated code

```

package com.intelygenz.android.test

import com.intelygenz.android.dsl.classpath.ScenarioRunner
import com.intelygenz.android.dsl.classpath.runScenario
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(ScenarioRunner::class)
class AllScenariosTest {

    @Test fun example_someDeterminableBusinessSituation() = runScenario("example.feature", "Some determinable business situation")

}

```

## Installation

On Android Studio up menu

Android Studio/preferences/plugin/{SETTINGS_SYMBOL}/Install Plugin from Disk

Find zip with the plugin.