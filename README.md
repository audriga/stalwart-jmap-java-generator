<div style="text-align: center;" align="center">

# Stalwart JMAP Java Generator

**[Schema Reference](https://stalw.art/docs/ref/)**

</div>

Generate extensions to the [jmap](https://codeberg.org/iNPUTmice/jmap) Java library from Stalwart's [schema.json](https://github.com/stalwartlabs/stalwart/blob/main/resources/schema/schema.json.gz) files.

Stalwart uses JMAP not only for the primary purpose of users interacting with the mail (+ calendar, contacts, etc.) server, but also for all administrative and maintenance tasks.
In fact, the entire [Admin UI](https://github.com/stalwartlabs/webui) is built on top of this JMAP interface.
Definitions for the Stalwart-specific JMAP data types are given entirely within a machine readable, custom schema format.

We generate Java source code from such a schema file, enabling usage of the the API via the [jmap](https://codeberg.org/iNPUTmice/jmap) library.

## WIP

This project is still under development and not yet ready for most use cases.

## License

Stalwart JMAP Java Generator is made available under the open source MIT license.
See [LICENSE](./LICENSE) for details.
