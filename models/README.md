# Menu name model

The production Docker image downloads and bundles the pinned DJL artifact for
[`intfloat/multilingual-e5-small`](https://huggingface.co/intfloat/multilingual-e5-small).
The model is licensed under Apache License 2.0 and is used only for local inference.

For local execution, extract the model archive into this directory so that the
following files exist:

```text
models/multilingual-e5-small/
  config.json
  multilingual-e5-small.pt
  serving.properties
  tokenizer.json
```

The archive URL and checksum are pinned in the root `Dockerfile`.
