#!/usr/bin/env python3
"""
文件分类工具

功能:
- 分类变更文件类型（Code/Doc/Test）
- 支持多语言（从 archetype-config.yml 读取配置）
"""

import re
from pathlib import Path
from typing import List, Dict
from enum import Enum

from .config_loader import load_config, get_config_value


class FileType(Enum):
    """文件类型枚举"""
    CODE = "code"
    DOC = "doc"
    TEST = "test"
    CONFIG = "config"
    UNKNOWN = "unknown"


# 默认扩展名（YAML 未配置时的 fallback）
DEFAULT_CODE_EXTS = {'.go', '.py', '.js', '.ts', '.java', '.c', '.cpp', '.h', '.hpp',
                     '.cs', '.php', '.rb', '.swift', '.kt', '.rs'}
DEFAULT_TEST_PATHS = ['/tests/', '\\tests\\']


def _load_file_patterns(cfg: dict) -> tuple:
    """从配置加载文件模式。返回 (code_exts, test_patterns, test_paths, config_exts)"""
    cfg_patterns = get_config_value(cfg, "spec_xchecker.file_patterns", {})

    # 代码扩展名
    source_patterns = cfg_patterns.get("source", ["**/*.go", "**/*.py", "**/*.java"])
    code_exts = set()
    for p in source_patterns:
        if p.startswith("**/*"):
            code_exts.add(Path(p).suffix.lower())

    # 测试文件模式
    test_patterns = cfg_patterns.get("test", ["*_test.go", "*Test.java", "test_*.py"])

    # 配置扩展名
    config_patterns = cfg_patterns.get("config", ["**/*.yaml", "**/*.yml", "**/*.json", "**/*.toml"])
    config_exts = set()
    for p in config_patterns:
        if p.startswith("**/*"):
            config_exts.add(Path(p).suffix.lower())

    return code_exts or DEFAULT_CODE_EXTS, test_patterns, DEFAULT_TEST_PATHS, config_exts or {'.yaml', '.yml', '.json', '.toml'}


# 模块级缓存
_cached_patterns = None


def _get_patterns():
    global _cached_patterns
    if _cached_patterns is None:
        try:
            cfg = load_config()
        except Exception:
            cfg = {}
        _cached_patterns = _load_file_patterns(cfg)
    return _cached_patterns


def classify_files(files: List[Path]) -> Dict[FileType, List[Path]]:
    """
    分类文件

    Args:
        files: 文件列表

    Returns:
        分类结果字典 {FileType: [文件列表]}
    """
    result = {
        FileType.CODE: [],
        FileType.DOC: [],
        FileType.TEST: [],
        FileType.CONFIG: [],
        FileType.UNKNOWN: [],
    }

    for file in files:
        file_type = _classify_single_file(file)
        result[file_type].append(file)

    return result


def _classify_single_file(file: Path) -> FileType:
    """
    分类单个文件

    Args:
        file: 文件路径

    Returns:
        文件类型
    """
    # 测试文件优先判断
    if _is_test_file(file):
        return FileType.TEST

    # 文档文件
    if _is_doc_file(file):
        return FileType.DOC

    # 配置文件
    if _is_config_file(file):
        return FileType.CONFIG

    # 代码文件
    if _is_code_file(file):
        return FileType.CODE

    return FileType.UNKNOWN


def _is_test_file(file: Path) -> bool:
    """判断是否为测试文件"""
    _, test_patterns, test_paths, _ = _get_patterns()

    # 检查文件路径
    path_str = str(file)
    for tp in test_paths:
        if tp in path_str:
            return True

    # 检查文件名是否匹配测试模式
    for pattern in test_patterns:
        # Convert glob pattern to regex
        regex = pattern.replace("*", ".*").replace("?", ".")
        if re.search(regex, file.name):
            return True

    return False


def _is_doc_file(file: Path) -> bool:
    """判断是否为文档文件"""
    doc_extensions = {
        '.md', '.txt', '.rst',
        '.pdf', '.doc', '.docx',
        '.wiki',
    }

    return file.suffix.lower() in doc_extensions


def _is_config_file(file: Path) -> bool:
    """判断是否为配置文件"""
    _, _, _, config_exts = _get_patterns()

    config_extensions = config_exts | {'.ini', '.cfg', '.conf', '.xml', '.sh', '.bash'}

    # 检查常见配置文件名
    config_filenames = {
        'dockerfile', 'makefile', 'gitignore',
        'docker-compose.yml', 'docker-compose.yaml',
    }

    return (
        file.suffix.lower() in config_extensions or
        file.name.lower() in config_filenames
    )


def _is_code_file(file: Path) -> bool:
    """判断是否为代码文件"""
    code_exts, _, _, _ = _get_patterns()

    return file.suffix.lower() in code_exts


# CLI 测试接口
if __name__ == '__main__':
    import sys

    if len(sys.argv) < 2:
        print("Usage: python classify.py <file1> [file2] ...")
        sys.exit(1)

    files = [Path(f) for f in sys.argv[1:]]
    result = classify_files(files)

    print("文件分类结果:")
    print(f"  代码文件 ({FileType.CODE.value}): {len(result[FileType.CODE])} 个")
    print(f"  文档文件 ({FileType.DOC.value}): {len(result[FileType.DOC])} 个")
    print(f"  测试文件 ({FileType.TEST.value}): {len(result[FileType.TEST])} 个")
    print(f"  配置文件 ({FileType.CONFIG.value}): {len(result[FileType.CONFIG])} 个")
    print(f"  未知文件 ({FileType.UNKNOWN.value}): {len(result[FileType.UNKNOWN])} 个")
    print()

    # 打印详细列表
    for file_type, files in result.items():
        if files:
            print(f"{file_type.value.upper()} 文件:")
            for file in files:
                print(f"  - {file}")
            print()
