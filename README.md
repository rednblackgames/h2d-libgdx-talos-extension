## HyperLap2D libGDX Talos Extension

HyperLap2D extension for libgdx runtime that adds [Talos](https://talosvfx.com) rendering support.

Currently based on custom [legacy fork](https://github.com/rednblackgames/talos-legacy) to maintain compatibility with v1 features.

### Integration

#### Gradle
![maven-central](https://img.shields.io/maven-central/v/games.rednblack.hyperlap2d/libgdx-talos-extension?color=blue&label=release)
![sonatype-nexus](https://img.shields.io/nexus/s/games.rednblack.hyperlap2d/libgdx-talos-extension?label=sanapshot&server=https%3A%2F%2Foss.sonatype.org)

Extension needs to be included into your `core` project.
```groovy
dependencies {
    api "games.rednblack.talos:runtime-libgdx:$talosVersion"
    api "games.rednblack.hyperlap2d:libgdx-talos-extension:$h2dTalosExtension"
}
```

#### Maven
```xml
<dependency>
  <groupId>games.rednblack.hyperlap2d</groupId>
  <artifactId>libgdx-talos-extension</artifactId>
  <version>0.1.4</version>
  <type>pom</type>
</dependency>
```

**Talos Runtime compatibility**

| HyperLap2D     | Talos  |
|----------------|--------|
| 0.1.5-SNAPSHOT | 1.5.0  |
| 0.1.4          | 1.5.0  |

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