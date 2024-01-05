# Ch02. 백엔드 개발자를 위한 클라우드 인프라 운영 - Amazon
- [1. Amazon 클라우드 사용하기(1) - 클라우드란](#ch03-01-amazon-클라우드-사용하기1---클라우드란)
- [1. Amazon 클라우드 사용하기(2) - AWS 계정 만들기](#ch03-02-amazon-클라우드-사용하기2---aws-계정-만들기)
- [1. Amazon 클라우드 사용하기(3) - IAM 사용자, 정책, 권한](#ch03-03-amazon-클라우드-사용하기3---iam-사용자-정책-권한)
- [1. Amazon 클라우드 사용하기(4) - 프리티어 서비스](#Ch03)


---------------------------------------------------------------------------------------------------------------------------
# Ch03-01. Amazon 클라우드 사용하기(1) - 클라우드란
## 클라우드란?
인터넷을 통해서 언제 어디서든 원하는 때에 원하는 만큼의 컴퓨터 자원을 손쉽게 사용할 수 있게 해주는 서비스
## 클라우드 서비스의 6가지 이점
1. 초기 선투자 비용 없음
2. 운영비용 절감
3. 탄력적인 운영 및 확장
4. 속도 및 민첩성
5. 비지니스에만 집중 가능
6. 글로벌 확장
## CSP(Cloud Service Provider, 클라우드 서비스 제공업체)
## AWS Cloud란
- 12년 연속 Gartner MQ(Magic Quadrant) Leader 선정
### AWS 리전과 가용 영역
- 리전(Region)
> AWS 서비스가 운영되는 지역으로 복수개의 데이터 센터들의 집합
- 가용영역(AZ)
> 리전내에 위치한 데이터 센터들
### AWS 글로벌 인프라
- 리전
- 가용영역
- 엣지 로케이션
> AWS의 CDN을 빠르게 제공(캐싱)하기 위한 거점
### AWS 서비스
- Applications
- Platform Services
- Foundation Services: Compute, Networking, Storage
- Infrastructure: Region, AZ, Edge Location
#### Tranditional Infrastructure - AWS
[Security] Firewalls, ACLs, Administrators - Security Group, NACLs, IAM
[Networking] Router, Network Pipeline, Switch - ELB, NLB, VPC
[Server] On-Promise Server - AMI, EC2 Instance
[Storage] DAS, SAN, NAS, RDBMS - EBS, EFS, S3, RDS


---------------------------------------------------------------------------------------------------------------------------
# Ch03-02. Amazon 클라우드 사용하기(2) - AWS 계정 만들기
## AWS 사용하기
- AWS 계정 생성 > root > MFA 인증 추가
## AWS 계정
- Root User
> 모든 권한을 가지는 계정
- IAM User
> 루트 계정내에 만들어진 계정
### AWS 계정(root)에 MFA 인증 추가
- 계정 > 보안 자격 증명 > MFA 할당 > MFA 디바이스 선택 - 인증 관리자 앱


---------------------------------------------------------------------------------------------------------------------------
# Ch03-03. Amazon 클라우드 사용하기(3) - IAM 사용자, 정책, 권한
## AWS 계정



---------------------------------------------------------------------------------------------------------------------------
# Ch03-04. Amazon 클라우드 사용하기(4) - 프리티어 서비스
## AWS 계정