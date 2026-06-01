#!/usr/bin/env python3
"""
archetype 全局配置加载器

所有 spec-xchecker 脚本从此模块读取技术栈配置，
不再硬编码语言、路径、命令等。

用法:
    from lib.config_loader import load_config, get_config_value

    cfg = load_config()
    test_pattern = get_config_value(cfg, "spec_xchecker.file_patterns.test")
"""

import os
from pathlib import Path
from typing import Any

try:
    import yaml
except ImportError:
    yaml = None


def find_config_file(start_dir: str | None = None) -> str | None:
    """
    从当前目录向上查找 archetype-config.yml。
    最多向上 5 级。
    """
    if start_dir is None:
        start_dir = os.getcwd()
    current = Path(start_dir).resolve()
    for _ in range(5):
        config_path = current / "archetype-config.yml"
        if config_path.exists():
            return str(config_path)
        current = current.parent
    return None


def load_config(project_dir: str | None = None) -> dict:
    """
    加载 archetype-config.yml。
    如果找不到配置文件，返回默认配置。
    """
    config_path = find_config_file(project_dir)

    if config_path is None:
        return _default_config()

    if yaml is None:
        raise ImportError(
            "PyYAML is required. Install with: pip install pyyaml"
        )

    with open(config_path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f) or _default_config()


def get_config_value(config: dict, path: str, default: Any = None) -> Any:
    """
    用点分隔路径从嵌套字典中取值。

    示例:
        get_config_value(cfg, "spec_xchecker.file_patterns.test")
        → config["spec_xchecker"]["file_patterns"]["test"]
    """
    keys = path.split(".")
    current = config
    for key in keys:
        if isinstance(current, dict) and key in current:
            current = current[key]
        else:
            return default
    return current


def _default_config() -> dict:
    """当找不到配置文件时使用的默认值（兼容旧 Go 项目）。"""
    return {
        "language": {
            "name": "go",
            "naming": {
                "file_suffix": ".go",
                "test_suffix": "_test.go",
                "identifier_case": "snake_case",
                "class_case": "PascalCase",
            },
        },
        "build_tool": {
            "name": "make",
            "commands": {
                "test": "go test -v ./...",
                "coverage": "go test -cover ./...",
            },
        },
        "directories": {
            "source_root": "internal",
            "layers": {
                "controller": "handler",
                "service": "logic",
                "repository": "dao",
                "entity": "model",
            },
        },
        "test": {
            "layers": {
                "ut": {
                    "file_pattern": "*_test.go",
                },
                "api": {
                    "file_pattern": "test_*.py",
                    "pytest_fallback": {
                        "dir": "tests/api",
                        "file_pattern": "test_*.py",
                    },
                },
                "sit": {
                    "file_pattern": "test_*.py",
                    "pytest_fallback": {
                        "dir": "tests/sit",
                        "file_pattern": "test_*.py",
                    },
                },
                "uat": {
                    "file_pattern": "test_*.py",
                    "pytest_fallback": {
                        "dir": "tests/uat",
                        "file_pattern": "test_*.py",
                    },
                },
            },
        },
        "spec_xchecker": {
            "file_patterns": {
                "source": ["**/*.go"],
                "test": ["*_test.go"],
                "config": ["**/*.yaml", "**/*.yml"],
            },
            "code_parsing": {
                "method_pattern": r"func\s+(\w+)\s*\(",
                "test_method_pattern": r"func\s+Test(\w+)\s*\(t\s+\*testing\.T\)",
            },
        },
        "quality": {
            "formatter": {"tool": "gofmt"},
            "linter": [{"tool": "golangci-lint"}],
        },
    }
