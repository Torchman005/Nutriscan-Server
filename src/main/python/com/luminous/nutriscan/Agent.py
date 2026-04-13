# Coze 工作流调用

import os
import sys
import logging
from datetime import datetime
from dotenv import load_dotenv
from cozepy import COZE_CN_BASE_URL, Coze, TokenAuth

# 配置日志
log_dir = "logs"
if not os.path.exists(log_dir):
    os.makedirs(log_dir)

log_filename = f"{log_dir}/agent_{datetime.now().strftime('%Y%m%d')}.log"

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_filename, encoding='utf-8'),
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger(__name__)

# 加载环境变量
load_dotenv()

# 验证环境变量
coze_api_token = os.getenv('COZE_API_KEY')
workflow_id = os.getenv('workflow_id2')

if not coze_api_token:
    logger.error("COZE_API_KEY environment variable not set")
    raise ValueError("COZE_API_KEY environment variable not set")
if not workflow_id:
    logger.error("workflow_id environment variable not set")
    raise ValueError("workflow_id environment variable not set")

# 配置API端点
coze_api_base = COZE_CN_BASE_URL

# 初始化Coze客户端
try:
    coze = Coze(auth=TokenAuth(token=coze_api_token), base_url=coze_api_base)
    logger.info("Coze client initialized successfully")
except Exception as e:
    logger.error(f"Failed to initialize Coze client: {e}")
    raise

def main():
    try:
        logger.info("Starting new workflow execution session")
        
        # 获取用户输入
        user_input = input("请输入菜品名称: (可选)").strip()
        logger.info(f"User input (dish name): {user_input if user_input else 'None'}")

        # 获取用户图片输入
        user_pic = input("请输入图片路径: ").strip()
        if not user_pic:
            logger.error("Image path is empty")
            print("错误：图片路径不能为空")
            return
        logger.info(f"User input (image path): {user_pic}")

        # 获取目标人群
        user_target = input("请输入目标人群: （可选）（默认：普通成年人）").strip()
        if not user_target:
            user_target = "普通成年人"
        logger.info(f"User input (target audience): {user_target}")

        # 准备工作流参数
        parameters = {
            'input': user_input,
            'pic': user_pic,
            'person': user_target
        }
        
        logger.info(f"Prepared workflow parameters: {parameters}")

        # 调用工作流
        print("正在执行工作流...")
        logger.info(f"Executing workflow with ID: {workflow_id}")
        
        start_time = datetime.now()
        workflow = coze.workflows.runs.create(
            workflow_id=workflow_id,
            parameters=parameters,
        )
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()

        logger.info(f"Workflow executed successfully in {duration:.2f} seconds")
        
        # 记录结果摘要（避免记录过大的数据）
        result_data = workflow.data
        logger.info(f"Workflow result received: {str(result_data)[:500]}...") # Log first 500 chars

        print("工作流执行结果:", result_data)

    except Exception as e:
        logger.error(f"An error occurred during execution: {str(e)}", exc_info=True)
        print(f"发生错误: {str(e)}")
        # import traceback
        # traceback.print_exc() # Logger handles traceback with exc_info=True


if __name__ == "__main__":
    main()