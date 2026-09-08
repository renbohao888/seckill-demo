# -*- coding: utf-8 -*-
"""
秒杀接口并发压测脚本（轻量版，无需安装 JMeter）。
用法：python tools/concurrency_test.py --n 200 --product 1
说明：默认 200 个不同用户并发打 POST /api/seckill，统计成功/失败原因/耗时/QPS。
依赖：仅需 Python 标准库（urllib / concurrent.futures），无需第三方包。
"""
import argparse
import json
import time
import urllib.request
import urllib.error
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE = "http://localhost:8081/api/seckill"


def one_request(product_id, user_id):
    """发送一次秒杀请求，返回 (code, msg)。"""
    url = "%s?productId=%s&userId=%s" % (BASE, product_id, user_id)
    req = urllib.request.Request(url, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            return data.get("code"), data.get("msg", "")
    except urllib.error.HTTPError as e:
        return e.code, str(e)
    except Exception as e:
        return -1, str(e)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--n", type=int, default=200, help="并发用户数")
    ap.add_argument("--product", type=int, default=1, help="商品ID")
    args = ap.parse_args()

    results = []
    start = time.time()
    with ThreadPoolExecutor(max_workers=args.n) as pool:
        futures = [pool.submit(one_request, args.product, i + 1) for i in range(args.n)]
        for f in as_completed(futures):
            results.append(f.result())
    elapsed = time.time() - start

    ok = sum(1 for c, _ in results if c == 200)
    fail = len(results) - ok
    # 按提示信息归因
    reason = {}
    for c, m in results:
        if c != 200:
            reason[m] = reason.get(m, 0) + 1

    print("=" * 50)
    print("并发请求总数 :", args.n)
    print("成功(code200) :", ok)
    print("失败         :", fail)
    print("失败原因分布 :")
    for m, cnt in sorted(reason.items(), key=lambda x: -x[1]):
        print("   - %s : %d" % (m, cnt))
    print("总耗时(s)    : %.3f" % elapsed)
    print("QPS(约)      : %.1f" % (args.n / elapsed if elapsed else 0))
    print("=" * 50)
    print("结论: 成功单量应 <= 商品库存; 若成功=%s 且无负数库存, 说明并发下不超卖。" % ok)


if __name__ == "__main__":
    main()
