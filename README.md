## HyperLap2D libGDX Talos Extension

HyperLap2D extension for libgdx runtime that adds Talos rendering support.

### Integration

#### Gradle
Release artifacts are available through ![Bintray](https://img.shields.io/bintray/v/rednblackgames/HyperLap2D/h2d-libgdx-talos-extension) 

Extension needs to be included into your `core` project.
```groovy
dependencies {
    api "com.talosvfx:talos-libgdx:$talosVersion"
    api "games.rednblack.editor:h2d-libgdx-talos-extension:$h2dTalosExtension"
}
```

#### Maven
```xml
<dependency>
  <groupId>games.rednblack.editor</groupId>
  <artifactId>h2d-libgdx-talos-extension</artifactId>
  <version>0.0.4</version>
  <type>pom</type>
</dependency>
```

**Spine Runtime compatibility**

| HyperLap2D         | Talos              |
| ------------------ | ------------------ |
| 0.0.4              | 1.3.1              |

### License
HyperLap2D's libGDX runtime Talos extension is licensed under the Apache 2.0 License. You can use it free of charge, without limitations both in commercial and non-commercial projects. We love to get (non-mandatory) credit in case you release a game or app using HyperLap2D!

```
Copyright (c) 2021 Francesco Marongiu.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.