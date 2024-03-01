# MineSec Headless SDK Integration Example

## Introduction

2 primary entities for Headless SDK consumer (aka Business App, client app, consumer app etc):

- `HeadlessSetup` an object for non-UI setup
    - Init SoftPOS: init MPoC/ MineHades with app & license
    - Initial Setups: load local default EMV, TERM, CAPK and get IK from remote
- `HeadlessActivity`: a base component class for consumer to impl
    - Provide default theme & UI: provide UI element for the card reading UI
    - Internally connected to Glue layer for `PoiRequest` handling

[API Definition here](https://theminesec.github.io/ms-sdk-headless/)

## Overview

![Overview](./docs/flow-overview.png)
