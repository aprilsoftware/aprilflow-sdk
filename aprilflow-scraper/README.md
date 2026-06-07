# April Flow - Scraper

This project uses the `aprilflow` Python SDK as an external library for all April Flow API calls.

## Dependencies

Install the April Flow SDK first. For local development from a sibling checkout:

```bash
pip install -e ../aprilflow-python-sdk
```

Then install the scraper:

```bash
pip install -e .
playwright install chromium
playwright install-deps chromium
```

If the SDK is published to your private package index or PyPI, install normally through the project dependency:

```bash
pip install -e .
```

The scraper declares this dependency in `pyproject.toml`:

```toml
dependencies = [
    "aprilflow>=0.1.0",
    "scrapy>=2.11.0",
    "scrapy-playwright>=0.0.34",
]
```

## Configuration

Required environment variables:

```bash
export APRILFLOW_BASE_URL="https://api.aprilflow.ai"
export APRILFLOW_USER_KEY="..."
```

Optional:

```bash
export SCRAPER_DATA_DIR="$HOME/scraper"
```

## Command line

```bash
scraper collections list
scraper collections get ab12cd34
scraper collections use ab12cd34

scraper scrape cssf --limit 20

scraper upload <run_dir> --into ab12cd34 --concurrency 2
```

## Shell

```bash
scraper shell

collections list
use ab12cd34
scrape cssf

upload ~/scraper/runs/2026-02-11_174642_cssf --concurrency 2

quit
```
